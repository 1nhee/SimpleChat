//https://github.com/1nhee/SimpleChat.git

import java.net.*;
import java.util.HashMap;
import java.util.Iterator;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class ChatServer {

	public static void main(String[] args) {
		try{
			//client�� ��û�� �ޱ� ���� ��Ʈ ��ȣ�� �Բ� ServerSocket �ν��Ͻ��� �����Ͽ� ������ �����Ѵ�.
			ServerSocket server = new ServerSocket(10001);
			//ServerSocket�� ���� server�� �������־���.
			System.out.println("Waiting connection...");
			//�������� ������ ���� HashMap�ν��Ͻ��� �����Ѵ�.
			//***HashMap�� while ��, Socket�� While�ȿ��� ����� ����?
			//������ ������ �ϴ� ������ �� client�� ���� ������ ���� �־�� �Ѵ�.(��ġ ���� ���� �ִ� ��ó�� client���� server�� �������ش�.) ������, HashMap�� ���� �����͸� �����ϴ� ������ �������� ���� �� �ֱ� ������ �ۿ��� �ѹ��� �����Ǿ �ȴ�. 
            HashMap<String, PrintWriter> hm = new HashMap<String, PrintWriter>();
	
               while(true){
			//while���� ���� ���� ����Ͽ� client�� ��û�� �޾Ƶ��δ�. (.accept)
			Socket sock = server.accept();
			//sock�ν��Ͻ��� ���� ���� client�� ��û�� chatthread�� �����Ͱ� ����� HashMap�� Chatthread�� �ѱ��.(�ּ� ������ �ѱ�)
			ChatThread chatthread = new ChatThread(sock, hm);
			chatthread.start();//run�� �����Ų��.
		} // end of while
		//������ ���� ���� ��Ȳ ��, e�� ����Ʈ �ǰ� �Ѵ�.
		}catch(Exception e){
			System.out.println(e);
		}
	} // end of main
}//end of ChatServer


class ChatThread extends Thread{// thread�� �����ͼ� start�� ����
	private Socket sock;
	private String id;
	private String id_myself;
	private BufferedReader br;
	private HashMap<String, PrintWriter> hm;
	private boolean initFlag = false;
	
	public ChatThread(Socket sock, HashMap<String, PrintWriter> hm){
		//reference ��, �ּ� ���� copy�ϴ� ������� �ν��Ͻ� ����
		this.sock = sock;
		this.hm = hm;

		try{
			//client���� �ƿ�ǲ �� ��������
			//PrintWriter Ŭ������ �־��� �����͸� ���� ������� �ٲپ� �ִ� ���̴�. �׷��Ƿ�, getOutputStream���κ��� return�� byte ������ �����͸� OutputStream���� ��������.
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
			//client���׼� �Է� �� �ޱ�
			//getInputStream�� return ���� byte�̴�. �׷��Ƿ�, �̸� ���� �Է� ��Ʈ����InputstreamReader�� �־��־�byte�� ��ǲ ������ ��ȯ�����ش�.(���۸����δ� int�� �ٲ��شٰ� �Ѵ�.) �׸���, buffered reader�� ���μ� �ϳ��� �о�� ���� �ƴ� ���� �����Ͱ� ���̸� �о�� ȿ�������� �����͸� �о���δ�. 
			//Id, ��ȭ ���� �� ������ ����ڷ��� �Է� ���� �о� ���� �� ����Ѵ�.
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			//br�� �о�� �����͸� id�� �־��ش�. (������� id�� �о���� ����)
			id = br.readLine();
			id_myself = id;
			broadcast(id + " entered.", id_myself);
			System.out.println("[Server] User (" + id + ") entered.");
			
			//synchronized�� �� �̻��� �����尡 ������ �ڿ��� �����ϴ� ���, ���� ���� �����尡 �ϳ��� �ڿ��� �����Ϸ��� �� �� �־��� �������� ���� �ϳ��� �����常�� ���� �����ϵ��� �Ѵ�.
			synchronized(hm){
				//HashMap�� ���� ��, (Key, Value) ���·� �����Ѵ�. 
				hm.put(this.id, pw);
			}
			initFlag = true;
			//���� �߻� �� ����
			}catch(Exception ex){
				System.out.println(ex);
			}
		} // end of constructor

		public void run(){
			try{
				String line = null;
				//����ڷκ��� ����Ͽ� ������ �о�´�.
				while((line = br.readLine()) != null){
					//���� ����ڰ� /quit�� �Է��ϸ� ����ڷκ��� �о���� ���� �����.
					if(line.equals("/quit"))
						break;
					//���� ����ڰ� /to�� �Է��ϸ� �ӼӸ� �޼ҵ尡 ����Ǿ� ���������� �޼����� ���� �� �ִ�.
					if(line.indexOf("/to ") == 0){ 
						sendmsg(line);
					}else if(line.indexOf("/userlist") == 0) {
						send_userlist();
					}else{
					//���� �ΰ��� ��� �̿ܿ� ��� ��ȭ ������ ������ ���� �������� broadcast�޼ҵ�� ��������.
					//Broadcast �޼ҵ�� ��� ������ ������ ��ȭ ������ �ԷµǴ� ���̴�.(��ü ä���� ����)
						broadcast(id + " : " + line, id_myself);
					}
				}
			//�̿ܿ� ������ �߻��ϸ� ex�� ��µȴ�.
			}catch(Exception ex){
				System.out.println(ex);
			//ä���� ������ ��, ä���� ����� �� 
			}finally{
			//�ϳ��� �ڿ��� �����Ϸ��� �� �� �־��� �������� ���� �ϳ��� �����常�� ���� �����ϵ��� �� �Ŀ� HashMap���� id�� �����.
				synchronized(hm){
				hm.remove(id);
				}
				//�� ä�ù濡 client ��, id�� ä�ù��� �������� �˸���.
				broadcast(id + " exited.", id_myself);
				try{
				//client�� ä�ù��� �������Ƿ� ���� ��, ������ �ݴ´�.
					if(sock != null)
					sock.close();
				//���ܰ� ���� ��� ex�� �����Ų��.
				}catch(Exception ex){}
			}//end of final
	} // end of run

		//�ӼӸ� ����� �����ϴ� �Լ�
		public void sendmsg(String msg){
			//start���� to���� ��, id�� ù��° �ε����� ����.
			int start = msg.indexOf(" ") +1;
			//end���� id ���� ��ĭ�� �ε��� ��ȣ�� ����.
			//indexof("char", num);�� num��°�� char�� �ǹ��Ѵ�. ��, ������ char�� num��° char�� �ε����� ã���ش�.
			int end = msg.indexOf(" ", start);
			
			//id�� �����Ѵٸ� end�� -1�� �ƴϹǷ�
			if(end != -1){
				//to���� id�� ����. 
				//substring�� start���� end������ ������ string�� �߶� �����Ѵ�.
				String to = msg.substring(start, end);
				//msg2���� end���� ��, �ӼӸ��ϰ��� �ϴ� ��ȭ ������ ����ȴ�.
				String msg2 = msg.substring(end+1);
				//to ��, id�� �ش��ϴ� value�� HashMap���κ��� �ҷ��´�. (HashMap���� id�� sock ��, ������ ����Ǿ� �ִ�.)
					Object obj = hm.get(to);
					//obj�� null�� �ƴϸ�, �� id�� �˸��� value�� ����(����)�� ������
					if(obj != null){
						PrintWriter pw = (PrintWriter)obj;
						//id�� msg2�� �ӻ迴�ٰ� ȭ�鿡 ����Ѵ�.
						pw.println(id + " whisphered. : " + msg2);
						//print�� ���� ���۰� ������ flush�� ���ش�.
						pw.flush();
					} // end of if
			}//end of second if
		} // end of sendmsg
		
		public void send_userlist() {
			Iterator<Entry<String, PrintWriter>> iterator = hm.entrySet().iterator();
			int numOfUsers = 0;
			
			System.out.println(); 
			System.out.println("In this Chat room, users are");
			
			while(iterator.hasNext()) {
				Entry entry = (Entry)iterator.next();
				PrintWriter pw = (PrintWriter)iterator.next();
				
				if(entry.getKey() != null) {
					numOfUsers++;
				}
				
				//msg�� ��� �濡 ����Ѵ�.
				String curr_id = "userID: " + entry.getKey();
				
				pw.println(curr_id);
				//print�� ���� ���۰� ������ flush�� ���ش�.
				pw.flush();
			}//end of while
		}//end of send_userlist
		
		//��� ä�ù濡 msg�� broadcast�ϴ� �޼ҵ�
		public void broadcast(String msg, String id_myself){
			//synchronized�� �� �̻��� �����尡 ������ �ڿ��� �����ϴ� ���, ���� ���� �����尡 �ϳ��� �ڿ��� �����Ϸ��� �� �� �־��� �������� ���� �ϳ��� �����常�� ���� �����ϵ��� �Ѵ�.
			synchronized(hm){
				Collection<PrintWriter> collection = hm.values();
				//iterator�� �÷����� �ִ� ����Ÿ�� �о� �˸´� ������ ã���ִ� �������̽��̴�. iterator�� ó������ ������ �ϳ��� ���������� ������ ���� �� �ۿ� ����.
				Iterator<PrintWriter> iter = collection.iterator();
				//iterator�� ������ �о� �� ��Ұ� ������ true�� ��ȯ�Ѵ�. ���� ��ȯ�� ��Ұ� ���ٸ� ��, �������� ���� �Ѿ�� false�� ��ȯ�Ѵ�.
				while(iter.hasNext()){
					//iterator�� ���� ���� pw�� �����Ѵ�.
					PrintWriter pw = (PrintWriter)iter.next();
					if(!pw.equals(id_myself)) {
							//msg�� ��� �濡 ����Ѵ�.
							pw.println(msg);
							//print�� ���� ���۰� ������ flush�� ���ش�.
							pw.flush();
					}//end of if
					}//end of while
				}//end of sync
			}//end of broadcast
	}//end of class