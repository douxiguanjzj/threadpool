package cn.jzj;

import java.sql.Connection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
/*CountDownLatch�÷���
 * public void await() throws InterruptedException { };   //����await()�������̻߳ᱻ��������ȴ�ֱ��countֵΪ0�ż���ִ��
public boolean await(long timeout, TimeUnit unit) throws InterruptedException { };  //��await()���ƣ�ֻ�����ȴ�һ����ʱ���countֵ��û��Ϊ0�Ļ��ͻ����ִ��
public void countDown() { };  //��countֵ��1
*/
public class ConnectionPoolTest {
   static ConnectionPool pool=new ConnectionPool(10);
   static CountDownLatch start=new CountDownLatch(1);
   static CountDownLatch end;
   public static void main(String[] args) throws InterruptedException {
	int threadCount=20;
	end=new CountDownLatch(threadCount);
	int count=20;
	AtomicInteger got=new AtomicInteger();
	AtomicInteger notgot=new AtomicInteger();
	for(int i=0;i<threadCount;i++) {
		Thread thread=new Thread(new ConnectionRunner(count,got,notgot),"sdfsdfs");
		thread.start();
	}
	start.countDown();
	end.await();
	System.out.println("total invoke:"+(threadCount*count));
	System.out.println("got connection:"+got);
	System.out.println("not got connection:"+notgot);
   }
   static class ConnectionRunner implements Runnable{
    int count;
    AtomicInteger got;
    AtomicInteger notGot;
    
	public ConnectionRunner(int count, AtomicInteger got, AtomicInteger notGot) {
		super();
		this.count = count;
		this.got = got;
		this.notGot = notGot;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			start.await();
		}catch(Exception e) {
			
		}
		while(count>0) {
			try {
				Connection connection=pool.fetchConnection(1000);
				if(connection!=null) {
					try {
					connection.createStatement();
					connection.commit();
					}finally {
						pool.releaseConnection(connection);
						got.incrementAndGet();
					}
				}else {
					notGot.incrementAndGet();
				}
			}catch(Exception e) {
				
			}finally {
				count--;
			}
		}
		end.countDown();
	}
	   
   }
}
