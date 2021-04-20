package chat7;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MultiServer {
	
	//멤버변수
	static ServerSocket serverSocket = null;
	static Socket socket = null;
	
	//클라이언트 정보저장을 위한 Map 컬렉션 생성
	Map<String, PrintWriter> clientMap;
	
	//생성자
	public MultiServer() {
		//클라이언트의 이름과 출력스트림을 저장할 HashMap 컬렉션 생성
		clientMap = new HashMap<String, PrintWriter>();
		//HashMap 동기화설정. 쓰레드가 사용자정보에 동시에 접근하는것을 차단함
		Collections.synchronizedMap(clientMap);
	}
	
	//채팅 서버 초기화
	public void init() {
		try {
			//서버 소켓 오픈
			serverSocket = new ServerSocket(9999);
			System.out.println("서버가 시작되었습니다.");
			
			/*
			1명의 클라이언트가 접속할때마다 접속을 허용(Accept)해주고
			동시에 MultiServerT 쓰레드를 생성한다.
			해당 쓰레드는 1명의 클라이언트가 전송하는 메세지를 읽어서 Echo
			해주는 역할을 담당한다.
			 */
			while(true) {
				//클라이언트의 접속 허가
				socket = serverSocket.accept();
				System.out.println(socket.getInetAddress()+"(클라이언트)의 "
						+socket.getPort()+"포트를 통해 "
						+socket.getLocalAddress()+"(서버)dml "
						+socket.getLocalPort()+"포트로 연결되었습니다.");
				
				//쓰레드로 정의된 내부클래스 객체생성 및 시작
				
				Thread mst = new MultiServerT(socket);
				mst.start();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				serverSocket.close();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/*
	chat4까지는 init()이 static이었으나, chat5부터는 일반적인
	멤버메소드를 변경된다. 따라서 객체를 생성후 호출하는 방식으로 변경된다.
	 */
	public static void main(String[] args) {
		MultiServer ms = new MultiServer();
		ms.init();

	}
	
	/*
	내부클래스
		: init()에 기술되었던 스트림을 생성후 메세지를 읽기/쓰기 하던
		부분이 해당 내부클래스로 이동되었다.
	 */
	class MultiServerT extends Thread {
		
		//멤버변수
		Socket socket;
		PrintWriter out = null;
		BufferedReader in = null;
		/*
		내부클래스의 생성자
			: 1명의 클라이언트가 접속할때 생성했던 Socket객체를
			매개변수로 받아 이를 기반으로 입출력 스트림을 생성한다.
		 */
		public  MultiServerT(Socket socket) {
			this.socket = socket;
			try {
				out = new PrintWriter(this.socket.getOutputStream());
				in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			}
			catch(Exception e) {
				System.out.println("예외:"+e);
			}
		}
		
		/*
		쓰레드로 동작할 run()에서는 클라이언트의 접속자명과
		메세지를 지속적으로 읽어 Echo해주는 역할을 한다.
		 */
		@Override
		public void run() {
			String name = "";
			String s = "";
			
			try {
				name = in.readLine();
				
				sendAllMsg("", name+"님이 입장하셨습니다.");
				
				clientMap.put(name, out);
				
				System.out.println(name +"접속");
				System.out.println("현재 접속자 수는"+clientMap.size()
				+"명 입니다.");
				
				while(in!=null) {
					s = in.readLine();
					if(s==null)
						break;
					
					System.out.println(name+" >> "+s);
					sendAllMsg(name, s);
				}
			}
			catch(Exception e) {
				System.out.println("예외:"+e);
			}
			finally {
				
				clientMap.remove(name);
				sendAllMsg("", name+"님이 퇴장하셨습니다.");
				System.out.println(name+"["+
				Thread.currentThread().getName()+"] 퇴장");
				System.out.println("현재 접속자 수는"
						+clientMap.size()+"명 입니다");
				try {
					in.close();
					out.close();
					socket.close();
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		//클라이언트 측으로 서버의 메세지를 Echo해주는 역할 담당
		public void sendAllMsg(String name, String msg)
		{
			Iterator<String> it = clientMap.keySet().iterator();
			
			while(it.hasNext()) {
				try {
					out.println("> "+ name +" ==> "+ msg);
				}
				catch(Exception e) {
					System.out.println("예외:"+e);
				}
			}
		}
	}

}
