import java.net.*;
import java.io.*;
import java.util.*;

public class ChatServer {

	public static void main(String[] args) {
		try{
			@SuppressWarnings("resource")
			ServerSocket server = new ServerSocket(10001);
			System.out.println("Waiting connection...");
			HashMap<String, PrintWriter> hm = new HashMap<String, PrintWriter>();
			while(true){
				Socket sock = server.accept();
				ChatThread chatthread = new ChatThread(sock, hm);
				chatthread.start();//run실행키는 중
			} // while
		}catch(Exception e){
			System.out.println(e);
		}
	} // main
}

class ChatThread extends Thread{// thread를 가져와서 start가 가능
	private Socket sock;
	private String id;
	private BufferedReader br;
	private HashMap<String, PrintWriter> hm;
	private boolean initFlag = false;
	class ChatThread extends Thread{
	private Socket sock;
	private String id;
	private BufferedReader br;
	private HashMap hm;
	private boolean initFlag = false;

	public ChatThread(Socket sock, HashMap hm){
//reference 즉, 주소 값을 copy하는 방식으로 인스턴스 생성
		this.sock = sock;
		this.hm = hm;

		try{
//client한테 아웃풋 값 내보내기
PrintWriter 클래스는 주어진 데이터를 문자 출력으로 바꾸어 주는 것이다.
그러므로, getOutputStream으로부터 return된 byte 단위의 데이터를 OutputStream으로 내보낸다.
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
//client한테서 입력 값 받기
getInputStream은 return 값이 byte이다. 그러므로, 이를 문자 입력 스트림인InputstreamReader에 넣어주어byte를 인풋 값으로 변환시켜준다.(구글링으로는 int로 바꿔준다고 한다.) 그리고, buffered reader로 감싸서 하나씩 읽어내는 것이 아닌 일정 데이터가 쌓이면 읽어내어 효율적으로 데이터를 읽어들인다. 
Id, 대화 내용 등 앞으로 사용자로의 입력 값을 읽어 들일 때에는 
				br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
//br로 읽어온 데이터를 id에 넣어준다. (사용자의 id를 읽어오는 것임)
				id = br.readLine();
				broadcast(id + " entered.");
				System.out.println("[Server] User (" + id + ") entered.");
//synchronized는 둘 이상의 쓰레드가 공동의 자원을 공유하는 경우, 여러 개의 쓰레드가 하나의 자원에 접근하려고 할 때 주어진 순간에는 오직 하나의 쓰레드만이 접근 가능하도록 한다.
				synchronized(hm){
//HashMap에 저장 시, (Key, Value) 형태로 저장한다. 
					hm.put(this.id, pw);
				}
				initFlag = true;
//에러 발생 시 실행
			}catch(Exception ex){
				System.out.println(ex);
			}
		} // end of constructor

//Q. run은 언제 실행되나?
		public void run(){
			try{
				String line = null;
//사용자로부터 계속하여 문장을 읽어온다.
				while((line = br.readLine()) != null){
//만약 사용자가 /quit을 입력하면 사용자로부터 읽어오는 것을 멈춘다.
					if(line.equals("/quit"))
						break;
//만약 사용자가 /to를 입력하면 귓속말 메소드가 실행되어 개인적으로 메세지를 보낼 수 있다.
					if(line.indexOf("/to ") == 0){ 
						sendmsg(line);
					}else
//위의 두가지 경우 이외에 모든 대화 내용은 다음과 같은 형식으로 broadcast메소드로 보내진다.
Broadcast 메소드는 모든 서버에 동일한 대화 내용이 입력되는 것이다.(전체 채팅을 위해)
						broadcast(id + " : " + line);
				}
//이외에 에러가 발생하면 ex가 출력된다.
			}catch(Exception ex){
				System.out.println(ex);
//채팅의 마지막
			}finally{
//하나의 자원에 접근하려고 할 때 주어진 순간에는 오직 하나의 쓰레드만이 접근 가능하도록 한 후에 HashMap에서 id를 지운다.
				synchronized(hm){
					hm.remove(id);
				}
//각 채팅방에 client 즉, id가 채팅방을 나갔음을 알린다.
				broadcast(id + " exited.");
				try{
//client가 채팅방을 나갔으므로 소켓 즉, 서버를 닫는다.
					if(sock != null)
						sock.close();
//예외가 생긴 경우 ex를 실행시킨다.
				}catch(Exception ex){}
			}
		} // end of run

//귓속말 기능을 실행하는 함수
		public void sendmsg(String msg){
//start에는 to이후 즉, id의 첫번째 인덱스가 담긴다.
			int start = msg.indexOf(" ") +1;
//end에는 id 뒤의 빈칸의 인데스 번호가 담긴다.
//indexof("char", num);은 num번째의 char를 의미한다. 즉, 동일한 char의 num번째 char의 인덱스를 찾아준다.
			int end = msg.indexOf(" ", start);
//
			if(end != -1){
				String to = msg.substring(start, end);
				String msg2 = msg.substring(end+1);
				Object obj = hm.get(to);//유저 이름에 붙어 있는 값이 나옴 
				if(obj != null){
					PrintWriter pw = (PrintWriter)obj;
					pw.println(id + " whisphered. : " + msg2);
					pw.flush();
				} // if
			}
		} // end of sendmsg
		public void broadcast(String msg){
			synchronized(hm){
				Collection collection = hm.values();
				Iterator iter = collection.iterator();
				while(iter.hasNext()){
					PrintWriter pw = (PrintWriter)iter.next();
					pw.println(msg);
					pw.flush();
				}
			}
		} // broadcast
		
		//'정말로 나가시겠습니까?' 와 같은거 물어보기
	}

