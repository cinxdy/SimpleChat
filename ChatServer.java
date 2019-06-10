import java.net.*;
import java.text.SimpleDateFormat;
import java.io.*;
import java.util.*;

public class ChatServer {

	public static void main(String[] args) {
		try{
			ServerSocket server = new ServerSocket(10001); //port번호 10001로 새로운 ServerSocket을 만든다
			System.out.println("Waiting connection..."); // Waiting connection...이라는 메세지를 터미널에 출력
			HashMap hm = new HashMap(); //새 해쉬맵을 만듦
			while(true){
				Socket sock = server.accept(); // sock라는 소켓은 server라는 서버소켓의 요청을 받아들인다.
				ChatThread chatthread = new ChatThread(sock, hm); // 새로운 ChatThread Object를 만든다.
				chatthread.start(); 
			} // while
		}catch(Exception e){
			System.out.println(e);
		}
	} // main
	
}

class ChatThread extends Thread{
	private Socket sock;
	private String id;
	private BufferedReader br;
	private HashMap hm;
	private boolean initFlag = false;


	public ChatThread(Socket sock, HashMap hm){
		this.sock = sock;
		this.hm = hm;
		try{
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream())); // sock의 OutputStream으로 PrintWriter를 만듦
			br = new BufferedReader(new InputStreamReader(sock.getInputStream())); // sock의 InputStream으로 Input Buffer를 만듦
			id = br.readLine(); //input buffer로 id를 읽음
			broadcast(id + " entered.");  
			System.out.println("[Server] User (" + id + ") entered."); //
			synchronized(hm){
				hm.put(this.id, pw);
			}
			initFlag = true;
		}catch(Exception ex){
			System.out.println(ex);
		}
	} // construcor


	public void run(){
		try{
			String line = null;
			while((line = br.readLine()) != null){
				if(line.equals("/quit"))
					break;
				if(line.indexOf("/to ") == 0){
					sendmsg(line);
				}else
					broadcast(id + " : " + line);
			}
		}catch(Exception ex){
			System.out.println(ex);
		}finally{
			synchronized(hm){
				hm.remove(id);
			}
			broadcast(id + " exited.");
			try{
				if(sock != null)
					sock.close();
			}catch(Exception ex){}
		}
	} // run


	public void sendmsg(String msg){
		int start = msg.indexOf(" ") +1;
		int end = msg.indexOf(" ", start);
		if(end != -1){
			String to = msg.substring(start, end);
			String msg2 = msg.substring(end + 1);
			Object obj = hm.get(to);
			if(obj != null){
				PrintWriter pw = (PrintWriter) obj;
				pw.println(id + " whisphered. : " + msg2);
				pw.flush();
			} // if
		}
	} // sendmsg


	public void broadcast(String msg){
		SimpleDateFormat s = new SimpleDateFormat("[a h:mm]");
		
		synchronized(hm){
			Collection collection = hm.values(); //Collection의 값은 HashMap이다.
			Iterator iter = collection.iterator(); //Iterator 만들기
			while(iter.hasNext()){	
				PrintWriter pw = (PrintWriter)iter.next();

				//#2
				pw.println(s.format(System.currentTimeMillis()));

				pw.println(msg); //pw outputStream으로 msg를 전송
				pw.flush(); // 버퍼 비우기
			}
		}

	} // broadcast


}
