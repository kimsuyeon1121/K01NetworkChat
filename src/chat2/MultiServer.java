package chat2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class MultiServer {

	public static void main(String[] args) {
		
		ServerSocket serverSoket = null;
		Socket socket = null;
		PrintWriter out = null;
		BufferedReader in = null;
		String s = ""; //클라이언트의 메세지를 저장
		String name = ""; //클라이언트의 이름을 저장
		
		try {
			//포트를 기반으로 서버에서 소켓을 열고 접속을 기다림
			serverSoket = new ServerSocket(9999);
			System.out.println("서버가 시작되었습니다.");
			
			//클라이언트의 접속을 허가
			socket = serverSoket.accept();
			System.out.println(socket.getInetAddress()+"(클라이언트)의 "
					+socket.getPort()+"포트를 통해 "
					+socket.getLocalAddress()+"(서버)dml "
					+socket.getLocalPort()+"포트로 연결되었습니다.");
			
			//메세지를 보낼준비(서버->클라이언트)
			out = new PrintWriter(socket.getOutputStream(), true);
			//메세지를 읽을(받을)준비(클라이언트->서버)
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			/*
			클라이언트가 서버로 전송하는 최초의 메세지는 "대화명" 이므로
			메세지를 읽은후 변수에 저장하고 클라이언트쪽으로 Echo해준다.
			 */
			if(in != null) {
				name = in.readLine(); //클라이언트의 이름을 읽어서 저장한다.
				System.out.println(name+" 접속"); //서버의 콘솔에 출력하고,
				out.println("> "+ name+"님이 접속했습니다."); //클라이언트 측으로 Echo해준다.
			}
			
			/*
			두번째 메세지부터는 실제 대화내용이므로 읽어와서 콘솔에 출력하고
			동시에 클라이언트 측으로 Echo해준다.
			 */
			while(in != null) {
				s = in.readLine();
				if(s==null) {
					break;
				}
				System.out.println(name+" ==> "+s); //서버의 콘솔에 출력
				out.println("> "+name+" ==> "+s); //클라이언트 측으로 Echo
			}
			
			System.out.println("Bye...!!!");
		}
		catch(Exception e) {
			System.out.println("예외1:"+e);
		}
		finally {
			try {
				in.close();
				out.close();
				socket.close();
				serverSoket.close();
			}
			catch(Exception e) {
				System.out.println("예외2:"+e);
			}
		}

	}

}
