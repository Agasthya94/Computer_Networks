/*
* @Author: Agasthya Vidyanath Rao Peggerla.
*/

import java.io.*;
import java.io.Serializable;
import java.util.Arrays;
class Packet implements Serializable {
	int seq_num;
	byte[] data;
	long check_sum;

	void setSnum(int s){
		this.seq_num = s;
	}
	int getSnum(){
		return this.seq_num;
	}
	
	void setData(byte[] da){
		this.data = da;
	}

	byte[] getData(){
		return this.data;
	}

	void setCheckSum(long cs){
		this.check_sum = cs;
	}

	long getCheckSum(){
		return this.check_sum;
	}


	@Override
	public String toString() {
		return "seq_no =" + seq_num + ", data=" + Arrays.toString(data)
				+ ", checksum=" + check_sum ;
	}

}