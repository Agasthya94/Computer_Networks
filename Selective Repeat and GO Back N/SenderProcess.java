/*
* @Author: Agasthya Vidyanath Rao Peggerla.
*/

import java.io.*;
import java.net.*;
import java.util.*;
import java.io.Serializable;

class SenderProcess{
	static ArrayList<Packet> all_packets = new ArrayList<Packet>();
	static ArrayList<Packet> packets_sent = new ArrayList<Packet>();
	static ArrayList<AckPacket> ack_recieved = new ArrayList<AckPacket>();

	
	public static void main(String[] args) throws Exception {

		double packetlossprob = 0.1;
		double checksumerror = 0.1;
		String protocol="";
		int m=0,window_size=0;
		int timeout=0;
		int segment_size=0;
		String fread;
		String filename = args[0];
		String portno = args[1];
		String no_packets = args[2];
		//Define Socket
		DatagramSocket sender_socket = new DatagramSocket();
      	InetAddress ipaddr = InetAddress.getByName("localhost");

      	int port_number = Integer.parseInt(portno);
      	int max_packet = Integer.parseInt(no_packets);

      	FileReader fr = new FileReader(filename);
      	BufferedReader br=new BufferedReader(fr);
      	int j=1;
      	while((fread = br.readLine())!=null){
      		switch(j){
      			case 1: protocol += fread;
      					break;
      			case 2: String[] line2 = fread.split(" ");
      					m = Integer.parseInt(line2[0]);
      					window_size = Integer.parseInt(line2[1]);
      					break;
      			case 3: timeout = Integer.parseInt(fread);
      					break;
      			case 4: segment_size = Integer.parseInt(fread);
      					break;
      		}
      		j++;
      	}
      	
      	//MAke all packets
      	for (int i =1 ;i<=max_packet ;i++ ) {
      		make_packet(i);
      	}
      	if (protocol.equals("GBN"))
      	{
      		      	int send_packno =0;
      	int rec_ackno=0;
      	int last_packno= all_packets.size()-1;
      	while(true){
      	while((send_packno - rec_ackno < window_size)&&(send_packno<=last_packno)){

      		//checksum error

      		if(checksumerror>Math.random())
      			all_packets.get(send_packno).setCheckSum(1234);

      		//send packets
      		//Serialize the packet into data(bytes)
      		ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutput out = null;
			try {
  			out = new ObjectOutputStream(bos);   
  			out.writeObject(all_packets.get(send_packno));
  			out.flush();
  			byte[] send_data = bos.toByteArray();
  			DatagramPacket send_packet = new DatagramPacket(send_data, send_data.length, ipaddr, port_number);
  			

  			//packet lost
  			if(packetlossprob<=Math.random()){


      		sender_socket.send(send_packet); //packet sent
      		System.out.println("Sending segment "+(send_packno+1));
      		packets_sent.add(all_packets.get(send_packno));
  				}
  				else{
  					System.out.println("Packet lost");
  				}
  			}
  				finally{
  			try {
    			bos.close();
  				} catch (IOException ex) {}

  			}
  		

  			send_packno++;

  		}
  			//deserialize the data(bytes) to packet

			byte[] ack_packet = new byte[1024];
      		DatagramPacket ack = new DatagramPacket(ack_packet, ack_packet.length);
      	
try{      		
          sender_socket.setSoTimeout(timeout);
          
      		sender_socket.receive(ack);
      		
			ByteArrayInputStream bis = new ByteArrayInputStream(ack.getData());
			ObjectInput in = null;
			try {
  			in = new ObjectInputStream(bis);
  			AckPacket ack_rec = (AckPacket)in.readObject(); 
  			
  			System.out.println("Ack received ack no = "+ack_rec.getSeq());
        if((!ack_recieved.isEmpty())&&(ack_recieved.get(ack_recieved.size()-1).getSeq()!=ack_rec.getSeq())){
  			ack_recieved.add(ack_rec);

  			rec_ackno = ack_rec.getSeq();
  			if(rec_ackno == max_packet){
  				break;
  			}
      }
      else if(ack_recieved.isEmpty()){
        ack_recieved.add(ack_rec);

        rec_ackno = ack_rec.getSeq();
      }
      else{
        System.out.println("Ack Discarded");
      }
  		
			} finally {
  			try {
    		if (in != null) {
      		in.close();
    			}
  			} catch (IOException ex) {}
			}
} catch(SocketTimeoutException e){
  System.out.println();
      System.out.println("Time out");
      send_packno = rec_ackno;
      while((send_packno - rec_ackno < window_size)&&(send_packno<=last_packno)){
          all_packets.get(send_packno).setCheckSum(cal_checksum(all_packets.get(send_packno).getData()));
          //checksum error

          if(checksumerror>Math.random())
            all_packets.get(send_packno).setCheckSum(1234);

          //send packets
          //Serialize the packet into data(bytes)
          ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutput out = null;
      try {
        out = new ObjectOutputStream(bos);   
        out.writeObject(all_packets.get(send_packno));
        out.flush();
        byte[] send_data = bos.toByteArray();
        DatagramPacket send_packet = new DatagramPacket(send_data, send_data.length, ipaddr, port_number);
        

        //packet lost
        if(packetlossprob<=Math.random()){


          sender_socket.send(send_packet); //packet sent
          System.out.println("seq no = "+all_packets.get(send_packno).getSnum());
          System.out.println("re-Sending segment "+(send_packno+1));
          packets_sent.add(all_packets.get(send_packno));

          }
          else{
            System.out.println("Packet lost");
          }
        }
          finally{
        try {
          bos.close();
          } catch (IOException ex) {}

        }
      

        send_packno++;

      }

}
      	

		}
      		
      	}

      	else if (protocol.equals("SR"))
      	{
      		int curr_index =0;
      	int end_index=window_size-1;
      	int last_packno= all_packets.size()-1;
        int ack_recieved[] = new int[all_packets.size()];
      	while(true){
      	while((curr_index<=end_index)&&(curr_index<=last_packno)){

      		//checksum error

      		if(checksumerror>Math.random())
      			all_packets.get(curr_index).setCheckSum(1234);

      		//send packets
      		//Serialize the packet into data(bytes)
      		ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutput out = null;
			try {
  			out = new ObjectOutputStream(bos);   
  			out.writeObject(all_packets.get(curr_index));
  			out.flush();
  			byte[] send_data = bos.toByteArray();
  			DatagramPacket send_packet = new DatagramPacket(send_data, send_data.length, ipaddr, port_number);
  			

  			//packet lost
  			if(packetlossprob<=Math.random()){


      		sender_socket.send(send_packet); //packet sent
      		System.out.println("Sending segment "+(curr_index+1));
      		
  				}
  				else{
  					System.out.println("Packet lost");
  				}
  			}
  				finally{
  			try {
    			bos.close();
  				} catch (IOException ex) {}

  			}
  		

  			curr_index++;

  		}
  			//deserialize the data(bytes) to packet

			byte[] ack_packet = new byte[1024];
      		DatagramPacket ack = new DatagramPacket(ack_packet, ack_packet.length);
      	
try{      		
          sender_socket.setSoTimeout(timeout);

      		sender_socket.receive(ack);
      		
			ByteArrayInputStream bis = new ByteArrayInputStream(ack.getData());
			ObjectInput in = null;
			try {
  			in = new ObjectInputStream(bis);
  			AckPacket ack_rec = (AckPacket)in.readObject(); 
  			
  			System.out.println("Ack received ack no = "+ack_rec.getSeq());  			
        ack_recieved[ack_rec.getSeq()-1] = 1;

			} finally {
  			try {
    		if (in != null) {
      		in.close();
    			}
  			} catch (IOException ex) {}
			}
} catch(SocketTimeoutException e){
  System.out.println();
      System.out.println("Time out");
      curr_index = end_index - window_size +1;
      for(int index = curr_index;index<=end_index;index++){

        if(ack_recieved[index]==0){

            all_packets.get(index).setCheckSum(cal_checksum(all_packets.get(index).getData()));
          //checksum error

          if(checksumerror>Math.random())
            all_packets.get(index).setCheckSum(1234);

          //send packets
          //Serialize the packet into data(bytes)
          ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutput out = null;
      try {
        out = new ObjectOutputStream(bos);   
        out.writeObject(all_packets.get(index));
        out.flush();
        byte[] send_data = bos.toByteArray();
        DatagramPacket send_packet = new DatagramPacket(send_data, send_data.length, ipaddr, port_number);
        
        //packet lost
        if(packetlossprob<=Math.random()){


          sender_socket.send(send_packet); //packet sent
          System.out.println("seq no = "+all_packets.get(index).getSnum());
          System.out.println("re-Sending segment "+(index+1));

          }
          else{
            System.out.println("Packet lost");
          }
        }
          finally{
        try {
          bos.close();
          } catch (IOException ex) {}

        }

        }

      }

      curr_index = end_index+1;
      end_index = end_index+window_size;
}
		}
      	}

	}
//function to calculate checksum
	static long cal_checksum(byte[] bs){
		byte s = 0;
		for (byte b :bs ) {
			s ^= b;
		}
		return s;
	}
	static void make_packet(int n){

		Packet p = new Packet();
		//generate random data
		Random r = new Random();
		int d = r.nextInt(10000) + 1;
		String s = Integer.toString(d);
		
		byte[] bdata = new byte[1024];
		bdata = s.getBytes();

		long chk_sum = cal_checksum(bdata);

		p.setSnum(n);
		p.setData(bdata);
		p.setCheckSum(chk_sum);

		all_packets.add(p);

	}
}