package at.innoc.roboat.radar;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

import at.innoc.roboat.radar.control.LiveControl;
import at.innoc.roboat.radar.helpers.IpHelper;

public class RadarLiveSource implements RadarSource, Runnable {

	private MulticastSocket socket;
	private DatagramPacket datagram;
	private static Thread keepAliveThread = null;
	private static boolean keepAliveStop;
	private static LiveControl control;

	public RadarLiveSource(LiveControl controlchannel) throws UnknownHostException, IOException {
		control = controlchannel;

		try {
			Object[] result = IpHelper.getMachineIp();
			if (result[0] == null) {
				throw new UnknownHostException((String) result[1]);
			} 
			InetAddress interfaceAddress = InetAddress.getByName((String) result[0]);
			NetworkInterface netfInf = NetworkInterface.getByInetAddress(interfaceAddress);
			socket = new MulticastSocket(6678);
			InetAddress group = InetAddress.getByName("236.6.7.8");
			socket.joinGroup(new InetSocketAddress(group, 6678), netfInf);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 

		datagram = new DatagramPacket(new byte[80000], 80000);
		keepAliveStop = false;
		if (keepAliveThread == null) {
			keepAliveThread = new Thread(this);
			keepAliveThread.setDaemon(true);
			keepAliveThread.start();
		}
	}

	/**
	 * gibt einen empfangenen Buffer zurück
	 */
	@Override
	public RadarDataFrame getNextDataFrame() {
		byte[] ret;
		try {
			socket.receive(datagram);
			long time = System.currentTimeMillis();
			ret = java.util.Arrays.copyOfRange(datagram.getData(), datagram.getOffset(), datagram.getLength());
			return new RadarDataFrame(ret, time);
		} catch (SocketException se) {
			System.out.println(se + "");
			return new RadarDataFrame();
		} catch (IOException e) {
			e.printStackTrace();
			return new RadarDataFrame();
		}
	}

	@Override
	public void close() {
		keepAliveStop = true;
		try {
			Object[] result = IpHelper.getMachineIp();
			if (result[0] == null) {
				throw new UnknownHostException((String) result[1]);
			} 
			InetAddress interfaceAddress = InetAddress.getByName((String) result[0]);
			NetworkInterface netfInf = NetworkInterface.getByInetAddress(interfaceAddress);
			InetAddress group = InetAddress.getByName("236.6.7.8");
			socket.leaveGroup(new InetSocketAddress(group, 6678), netfInf);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		socket.close();

	}

	/**
	 * Sends keep alive packages
	 */
	@Override
	public void run() {
		while (!keepAliveStop) {
			try {
				Thread.sleep(1000);
				control.sendKeepAlive();
			} catch (Exception e) {
			}
		}
	}

}
