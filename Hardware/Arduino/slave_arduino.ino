#include <Wire.h>//声明I2C库文件
#include "SR04.h"

#define ECHO_PIN1 2
#define TRIG_PIN1 3
#define ECHO_PIN2 4
#define TRIG_PIN2 5
#define ECHO_PIN3 6
#define TRIG_PIN3 7

#define ECHO_PIN4 8
#define TRIG_PIN4 9
#define ECHO_PIN5 10
#define TRIG_PIN5 11
#define ECHO_PIN6 12
#define TRIG_PIN6 13

#define ECHO_PIN7 14
#define TRIG_PIN7 15
#define ECHO_PIN8 16
#define TRIG_PIN8 17
#define ECHO_PIN9 20
#define TRIG_PIN9 21

SR04 sr1 = SR04(ECHO_PIN1,TRIG_PIN1);
SR04 sr2 = SR04(ECHO_PIN2,TRIG_PIN2);
SR04 sr3 = SR04(ECHO_PIN3,TRIG_PIN3);
//SR04 sr4 = SR04(ECHO_PIN4,TRIG_PIN4);
//SR04 sr1 = SR04(ECHO_PIN1,TRIG_PIN1);
//SR04 sr6 = SR04(ECHO_PIN6,TRIG_PIN6);
//SR04 sr7 = SR04(ECHO_PIN7,TRIG_PIN7);
//SR04 sr8 = SR04(ECHO_PIN8,TRIG_PIN8);
SR04 sr9 = SR04(ECHO_PIN9,TRIG_PIN9);

short x1=0xff00;
short x2=0x00ff;
short t[9];
byte arr[18];  
//初始化
void setup()
{
  Wire.begin(4);                // 加入 i2c 总线，设置从机地址为 #4
  Wire.onReceive(receiveEvent); //注册接收到主机字符的事件
  Wire.onRequest(requestEvent); // 注册主机通知从机上传数据的事件
  Serial.begin(9600);           //设置串口波特率

}
//主程序
void loop()
{
      for(int i=0;i<9;++i){
           t[i]=300;
      }
      t[0]=sr1.Distance();
      t[1]=sr2.Distance();
      t[2]=sr3.Distance();
     // t[3]=sr4.Distance();
      //t[4]=sr1.Distance();
      //t[5]=sr6.Distance();
      //t[6]=sr7.Distance();
      //t[7]=sr8.Distance();
      t[8]=sr9.Distance();

      Serial.print(t[0]);
      Serial.print(" ");
      Serial.print(t[1]);
      Serial.print(" ");
      Serial.print(t[2]);
      Serial.print(" ");
      Serial.print(t[3]);
      Serial.print(" ");
      Serial.print(t[4]);
      Serial.print(" ");
      Serial.print(t[5]);
      Serial.print(" ");
      Serial.print(t[6]);
      Serial.print(" ");
      Serial.print(t[7]);
      Serial.print(" ");
      Serial.print(t[8]);
      Serial.println("");
                
      for(int i=0;i<9;++i){
           arr[i*2]=(byte)(t[i]&x2);
           arr[i*2+1]=(byte)((t[i]&x1)>>8);
             //Serial.println(arr[i*2]);
             //Serial.println(arr[i*2+1]);
      }   
      //delay(100);//延时
}



//当主机通知从机上传数据，执行该事件
void requestEvent()
{

  //把接收主机发送的数据包中的最后一个字节再上传给主机
  Wire.write(arr,18);
}


// 当从机接收到主机字符，执行该事件
void receiveEvent(int howMany)
{
  while( Wire.available()>1) // 循环执行，直到数据包只剩下最后一个字符
  {
    char c = Wire.read(); // 作为字符接收字节
    Serial.print(c);         // 把字符打印到串口监视器中
  }
   //接收主机发送的数据包中的最后一个字节
//  x = Wire.read();    // 作为整数接收字节
  char t='a';
  //Wire.write("bbbbb");
//  Serial.println(x);    //把整数打印到串口监视器中，并回车 
}
