/*
* @Author: Agasthya Vidyanath Rao Peggerla.
*/

import java.io.*;
import java.io.Serializable;

class AckPacket implements Serializable{
	int seq_no;

	
	void setSeq(int se){
		this.seq_no = se;
	}

	int getSeq(){
		return this.seq_no;
	}

	@Override
	public String toString() {
		return "seq_no =" + seq_no;
	}

}