/*
* @Author: Agasthya Vidyanath Rao Peggerla.
*/

import java.io.*;
import java.net.*;
import java.util.*;


class ReceiverProcess{
	static ArrayList<Packet> received_packets = new ArrayList<Packet>();
	static ArrayList<AckPacket> ack_sent = new ArrayList<AckPacket>();

	public static void main(String[] args) throws Exception{
		String pno = args[0];
    String protocol = args[1];
		int portno = Integer.parseInt(pno);
		DatagramSocket receiver_socket = new DatagramSocket(portno);

		byte[] receive_data = new byte[1024];	
		int expected_sqno=1;
		int exp_ackno = 1;
		double acklossprob = 0.05;

		if(protocol.equals("GBN"))
		{
			
		while(true){
			DatagramPacket receive_packet = new DatagramPacket(receive_data, receive_data.length);
            receiver_socket.receive(receive_packet);
            InetAddress ipaddr = receive_packet.getAddress();
            int port = receive_packet.getPort();


            //Deserialize
            ByteArrayInputStream bi = new ByteArrayInputStream(receive_packet.getData());
			      ObjectInput in = null;
			try {
  			   in = new ObjectInputStream(bi);
  		
  			   Packet pack_rec = (Packet)in.readObject(); 

  			   if((expected_sqno == pack_rec.getSnum())){

  				    if(pack_rec.getCheckSum()==cal_checksum(pack_rec.getData())){
      		      received_packets.add(pack_rec);
      		      System.out.println("packet "+pack_rec.getSnum()+" received");
  			        make_AckPacket(pack_rec.getSnum());
  			        expected_sqno = ack_sent.get(ack_sent.size()-1).getSeq()+1;
  			       }
  			      else{
  				      System.out.println("Checksum error");
                System.out.println();
                System.out.println("last acked "+ack_sent.get(ack_sent.size()-1).getSeq());
                System.out.println("expected_sqno = "+expected_sqno);
                System.out.println("pack_rec = "+pack_rec.getSnum());
                System.out.println();
				      } 
  			}
        else{
  				System.out.println("Packet arrived out of order. Some packet/s may be lost");
          System.out.println();
          System.out.println("last acked "+ack_sent.get(ack_sent.size()-1).getSeq());
          System.out.println("expected_sqno = "+expected_sqno);
          System.out.println("pack_rec = "+pack_rec.getSnum());
          System.out.println();
  			}
			}
			finally {
  			try {
    		if (in != null) {
      		in.close();
    			}
  			} catch (IOException ex) {}
			}

      		
			//serialize

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutput out = null;
			try {
				if(acklossprob<=Math.random()){
  			out = new ObjectOutputStream(bos);  

  			out.writeObject(ack_sent.get(ack_sent.size()-1));
  			out.flush();
  			byte[] send_data = bos.toByteArray();
  			DatagramPacket send_ACKpacket = new DatagramPacket(send_data, send_data.length, ipaddr, port);
        System.out.println("ack "+ack_sent.get(ack_sent.size()-1).getSeq()+" sent");
      		receiver_socket.send(send_ACKpacket); //packet sent
  			}
  			else{
  				System.out.println("Ack lost");

  				//ack_sent.remove(ack_sent.size()-1);
  				if(!ack_sent.isEmpty()){
  				expected_sqno = ack_sent.get(ack_sent.size()-1).getSeq()+1;}
  				else{
  					expected_sqno = 1;
  				}

  			}
  				}

  				finally{
  			try {
    			bos.close();
  				} catch (IOException ex) {}

  			}

      		//receiver_socket.send(ack_sent[ack_sent.size()-1]);

		}
		}
		else if(protocol.equals("SR"))
		{
			int flag=0;

		while(true){
			DatagramPacket receive_packet = new DatagramPacket(receive_data, receive_data.length);
            receiver_socket.receive(receive_packet);
            InetAddress ipaddr = receive_packet.getAddress();
            int port = receive_packet.getPort();


            //Deserialize
            ByteArrayInputStream bi = new ByteArrayInputStream(receive_packet.getData());
			ObjectInput in = null;
			try {
  			in = new ObjectInputStream(bi);
  			

  			Packet pack_rec = (Packet)in.readObject(); 

  				if(pack_rec.getCheckSum()==cal_checksum(pack_rec.getData())){
      		System.out.println("packet "+pack_rec.getSnum()+" received");
  			make_AckPacket(pack_rec.getSnum());
        flag=0;

  			}
  			else{
          flag=1;
  			//	System.out.println("Checksum error");
          //System.out.println();
          System.out.println("pack_rec = "+pack_rec.getSnum());
          System.out.println();
				} 
  		
			}
			finally {
  			try {
    		if (in != null) {
      		in.close();
    			}
  			} catch (IOException ex) {}
			}

      		
			//serialize

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutput out = null;
			try {
				if(acklossprob<=Math.random() && flag==0){
  			out = new ObjectOutputStream(bos);  

  			out.writeObject(ack_sent.get(ack_sent.size()-1));
  			out.flush();
  			byte[] send_data = bos.toByteArray();
  			DatagramPacket send_ACKpacket = new DatagramPacket(send_data, send_data.length, ipaddr, port);
        System.out.println("ack "+ack_sent.get(ack_sent.size()-1).getSeq()+" sent");
      		receiver_socket.send(send_ACKpacket); //packet sent
  			}
        else if(flag==1){
          System.out.println("Checksum error ack not sent");
        }
  			else{
  				System.out.println("Ack lost");

  			}
  				}

  				finally{
  			try {
    			bos.close();
  				} catch (IOException ex) {}

  			}

		}
		}
	}

	static void make_AckPacket(int n){
		AckPacket ap = new AckPacket();

		ap.setSeq(n);
		
		ack_sent.add(ap);
	}


	static long cal_checksum(byte[] bs){
		byte s = 0;
		for (byte b :bs ) {
			s ^= b;
		}
		return s;
	}
}