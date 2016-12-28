#pragma  comment(lib,"ws2_32.lib")
#include <stdio.h>
#include<iostream>
//#include<opencv2\opencv.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <WINSOCK2.H>

#define DEST_IP "172.16.0.207"//"192.168.23.4"//"223.3.93.59"
#define PORT 8888
#define INVALID_SOCKET -1
#define SOCKET_ERROR -1
#define BUFF_SIZE 1024 * 4//any number you wants according to your OS
#define HDRLEN_IPLIMAGE sizeof(IplImage)
#define SENSOR_NUM 9

using namespace std;
using namespace cv;

typedef unsigned char byte;

struct hdr_data
{
	long length;
	long data_len;
};
#define HDRLEN_DATA sizeof(hdr_data)
bool SendIplImage(IplImage* source, int s)
{
	int ret, count, datalen;
	char *index;
	hdr_data datainfo;
	char sync[2] = { 0, 0 };
	index = source->imageData;
	count = 0;
	//cout<<"1"<<endl;
	ret = send(s, (char *)source, HDRLEN_IPLIMAGE, 0);//----1 send header
	//cout <<"source:"<< (char *)source << endl;
	datalen = BUFF_SIZE;
	//cout<<"2"<<endl;
	while (ret > 0){
		datainfo.data_len = datalen;
		ret = send(s, (char *)&datainfo, HDRLEN_DATA, 0);
		ret = send(s, (char *)index, datalen, 0);
		if (datalen < BUFF_SIZE)//the last left data has been sent
			break;
		count += datalen;
		index += datalen;
		if (count > source->imageSize - BUFF_SIZE)//last left data's length less than BUFF_SIZE
			datalen = source->imageSize - count;//left's lenth=total length-sent's length
	}
	datainfo.data_len = -1;
	ret = send(s, (char *)&datainfo, HDRLEN_DATA, 0);
	//system("pause");
	return 1;
}
int main(int argc, char* argv[])
{

	WSADATA wsd;
	SOCKET sockClient; //client socket
	SOCKADDR_IN addrSrv;
	char recvBuf[100];
	if (WSAStartup(MAKEWORD(2, 2), &wsd) != 0)
	{
		printf("start up failed!\n");
		system("pause");
		return 0;
	}

	SOCKET sclient = socket(AF_INET, SOCK_STREAM, 0);

	sockaddr_in serAddr;
	serAddr.sin_family = AF_INET;
	serAddr.sin_port = htons(PORT);
	//serAddr.sin_addr.s_addr = inet_addr(DEST_IP);
	serAddr.sin_addr.S_un.S_addr = inet_addr(DEST_IP);
	if (connect(sclient, (sockaddr *)&serAddr, sizeof(serAddr)) == SOCKET_ERROR){
		cout << "invalid socket connect ip!" << endl;
		closesocket(sclient);
		return 0;
	}

	byte *a=new byte[SENSOR_NUM*2];
	for (int i = 0; i < 18; ++i){
		if (i % 2 == 0)
			a[i] = 1;
		else{
			a[i] = 44 + (byte)i / 2;
		}
	}

	//read image and send
	IplImage *image_src = NULL;
	//IplImage *image_dst = cvCreateImage(cvSize(640, 480), 8, 1);//我的摄像头是640x480的，如不同则自行修改
	/*CvCapture *capture = cvCreateCameraCapture(0);//打开摄像头
	if (!capture){
		cout << "open camera failed!" << endl;
	}*/
	VideoCapture capture(0);
	while (1){	

		send(sclient, (char*)a, SENSOR_NUM * 2, 0);

		Mat frame;
		capture >> frame;
		image_src = &((IplImage)frame);

		//image_src = cvQueryFrame(capture);
		cout << image_src->width << " " << image_src->height << " " << image_src->imageSize << endl;
		cvShowImage("client", image_src);
		if (!SendIplImage(image_src, sclient))
			break;
 		cvWaitKey(30);
	}
	cvReleaseImage(&image_src);
	//cvReleaseImage(&image_dst);
	//cvReleaseCapture(&capture);
	closesocket(sclient);
	delete[]a;

	system("pause");
	return 0;

}