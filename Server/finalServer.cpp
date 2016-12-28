#pragma  comment(lib,"ws2_32.lib")
#define  _CRT_SECURE_NO_WARNINGS
#include <stdio.h>
#include<iostream>
#include <WINSOCK2.H>
#include <Windows.h>
#include<opencv2\opencv.hpp>

#include <SDKDDKVer.h>
//#include <sys/types.h>
//#include <sys/socket.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
//#include <netinet/in.h>
//#include <arpa/inet.h>
//#include <unistd.h>
#include <fcntl.h>
#include <errno.h>
//#include <sys/shm.h>
//#include <pthread.h>
//#include <sys/syscall.h>
#include <cstring>
#include <iostream>
#include <sstream>
#include <opencv2/opencv.hpp>
#include <math.h>
#include <pthread.h>  
#include <assert.h>  

//#define _CRT_SECURE_NO_WARNNINGS

#pragma comment(lib,"x86/pthreadVC2.lib")  
using namespace std;
using namespace cv;

#define PORT_EDISON            8888
#define PORT_RECV_FROM_APP     9777
#define PORT_SEND_TO_APP       9888
//#define ADDRESS_APP            "0.0.0.0"
#define QUEUE                  20
#define QUEUE1                 20
#define BUFFER_SIZE            1024 * 4
#define HDRLEN_IPLIMAGE        112
#define SENSOR_NUM             9
#define PI                     3.141592653
#define HEIGHT                 170             // Height
#define WAIST_HEIGHT           100             // Lower body length
#define ANGLE_HEAD             30
#define ANGLE_WAIST1           75              // 腰部向下角度1
#define ANGLE_WAIST2           37   	       // 腰部向下角度2

struct hdr_data
{
	//int length;
	int data_len;
};
#define HDRLEN_DATA            sizeof(hdr_data)

// Header
//bool receivedata(char* buf, int length, int sockfd);
bool receivedata(char* buf, int length, SOCKET s);
IplImage* RcvIplImage(SOCKET s);
//bool RcvIplImage(IplImage* dst, SOCKET s);
// for sensor detect
int obstacleDetect(int* data);
//int min(int a, int b);
void dataToDecimal(char* src, int* dst);
int leftDetect(int);
int rightDetect(int);
int frontDetect(int);
int upstairDetect(int, int);
int downstairDetect(int);
int chestDetect(int, int, int);
int waistDetect(int, int, int);
int kneeDetect(int, int, int);
// for traffic light detect
void trafficLightHandler(IplImage*);
int processImgR(Mat);
int processImgG(Mat);
bool isIntersected(Rect, Rect);
void cross(bool);
// for road detect
bool detectPix(Vec3b& target, const Vec3b& origin);
void colorReduce(const Mat& image, Mat& outImage);
void roadHandler(IplImage*);
void clearCount();

void* imgThreadFunc(void*);
void send2App(int, int);

/********************
* Global variables
*******************/
// sensor detect
int leftCount = 0;
int rightCount = 0;
int upCount1 = 0;
int upCount2 = 0;
int downCount1 = 0;
int downCount2 = 0;
int frontCount = 0;
int chestCount = 0;
int waistCount = 0;
int kneeCount = 0;
int upGap = 0;
int downGap = 0;
bool isBegining = true;
bool beginUpstair = false;
bool beginDownstair = false;
bool isFirstUp = true;
bool isFirstDown = true;
bool isUpstair = false;
int beginCount = 0;
int height = 0;
int state = -1;

// accepted image
IplImage* image_src = cvCreateImage(cvSize(640, 480), 8, 3);

bool isRoad = true;

// traffic light detect
bool isFirstDetectedR = true;
bool isFirstDetectedG = true;
Rect* lastTrackBoxR;
Rect* lastTrackBoxG;
int lastTrackNumR;
int lastTrackNumG;
int firstSignal = -1; // -1->initial, 0->red, 1->green
bool hasRed = false; // used in the occasion that the first signal is green
bool canPass = false;

// road detect
Mat roi, src, src_gray, dst, ttt;
int nFrmNum = 0;
int edgeTimes = 0;
const int times = 5;
int rightEdge = 600;

SOCKET slisten;
SOCKET cSocket;
SOCKET sSocket;
// send to app
stringstream ss;
int sensorRes = -1;
int cameraRes = -1; // -1->NULL, 0->Red, 1->Pass, 2->Right

//// socket
//int server_sockfd_app;
//struct sockaddr_in server_sockaddr_app;
SOCKET conn_app_server;

//SOCKET client_sockfd_app;
//struct sockaddr_in client_sockaddr_app;
SOCKET conn_app_client;
char buffer1[4];

int main(void)
{


	//init WSA
	WORD sockVersion = MAKEWORD(2, 2);
	WSADATA wsaData;
	if (WSAStartup(sockVersion, &wsaData) != 0){
		return 0;
	}
//////////////////
	cSocket = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);//创建了可识别套接字
	//if (cSocket == -1){
	//	cout << "invalid socket" << endl;
	//}

	if (cSocket == INVALID_SOCKET){
		cout << "socket1 error !" << endl;
		return 0;
	}

	SOCKADDR_IN client_addr;
	client_addr.sin_family = AF_INET;
	client_addr.sin_port = htons(PORT_SEND_TO_APP);//绑定端口
	client_addr.sin_addr.S_un.S_addr = htonl(INADDR_ANY);//ip地址
	if (bind(cSocket, (LPSOCKADDR)&client_addr, sizeof(client_addr)) == SOCKET_ERROR){
		cout << "bind error1 !" << endl;
		cout << WSAGetLastError() << endl;
	}//绑定完成
	//start listening
	if (listen(cSocket, 1) == SOCKET_ERROR){
		cout << "listen error1 !" << endl;
		return 0;
	}

	int len = sizeof(SOCKADDR);
	SOCKADDR_IN clientsocket;

	sSocket = socket(AF_INET, SOCK_STREAM, 0);//创建了可识别套接字
	//if (sSocket == -1){
	//	cout << "invalid socket" << endl;
	//}
	if (sSocket == INVALID_SOCKET){
		cout << "socket2 error !" << endl;
		return 0;
	}


	//需要绑定的参数
	SOCKADDR_IN server_addr;
	server_addr.sin_family = AF_INET;

	server_addr.sin_port = htons(PORT_RECV_FROM_APP);//绑定端口
	server_addr.sin_addr.S_un.S_addr = htonl(INADDR_ANY);//ip地址
	if (bind(sSocket, (LPSOCKADDR)&server_addr, sizeof(server_addr)) == SOCKET_ERROR){
		cout << "bind error2 !" << endl;
	}//绑定完成

	if (listen(sSocket, 5) == SOCKET_ERROR){
		cout << "listen error2 !" << endl;
		return 0;
	}

	//char buffer1[4];
	int len1 = sizeof(SOCKADDR);
	SOCKADDR_IN serversocket;

	cout << "Waiting for connection to App111111111111..." << endl;
	conn_app_client = accept(cSocket, (SOCKADDR*)&clientsocket, &len);//如果这里不是accept而是conection的话。。就会不断的监听

	cout << "Connection to app accepted111111111111!" << endl;
//////////////////////
	
	//struct sockaddr_in client_addr_app;
	//socklen_t length_app = sizeof(client_addr_app);

	// Connect，return an non-negative descriptor if success, else -1
	cout << "Waiting for connection to App222222222222..." << endl;
	conn_app_server = accept(sSocket, (SOCKADDR*)&serversocket, &len1);
	if (conn_app_server < 0)
	{
		perror("connect");
		exit(1);
	}
	cout << "Connection to app accepted22222222222222!" << endl;

///////////////////////
	//create socket
	slisten = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
	if (slisten == INVALID_SOCKET){
		cout << "socket error !" << endl;
		return 0;
	}

	//bind ip and port
	sockaddr_in sin;
	sin.sin_family = AF_INET;
	sin.sin_port = htons(PORT_EDISON);//port8888
	sin.sin_addr.S_un.S_addr = INADDR_ANY;
	if (bind(slisten, (LPSOCKADDR)&sin, sizeof(sin)) == SOCKET_ERROR){
		cout << "bind error0 !" << endl;
	}
	//start listening
	if (listen(slisten, 5) == SOCKET_ERROR){
		cout << "listen error0 !" << endl;
		return 0;
	}

	//loop receiving data
	SOCKET sClient;
	sockaddr_in remoteAddr;
	int nAddrlen = sizeof(remoteAddr);
	cout << "wait connecting edison..." << endl;
	do{
		sClient = accept(slisten, (SOCKADDR *)&remoteAddr, &nAddrlen);
	} while (sClient == INVALID_SOCKET);
	cout << "accept edison connection" << endl;// inet_ntoa(remoteAddr.sin_addr) << endl;
	//cout<<"05"<<endl;
	//accepted image's size is 640*480

	pthread_t imgThread;
	int r = pthread_create(&imgThread, NULL, imgThreadFunc, NULL);
	if (r != 0)
	{
		cout << "pthread_create error: error_code=" << r << endl;
	}

	char revData[1000000] = "";
	int i, j;
	int ret;
	char* recvData = new char[SENSOR_NUM * 2];
	int* sensorData = new int[SENSOR_NUM];
	int count = 25;

	cvNamedWindow("server", 1);

	while (1) {
		int len = recv(sClient, recvData, SENSOR_NUM * 2, 0);
		cout << "receive:" << count << "   " << len << " data." << endl;
		dataToDecimal(recvData, sensorData);
		for (int i = 0; i < SENSOR_NUM; ++i)
			printf("%d ", sensorData[i]);
		printf("\n");
		++count;
		// deal with datas from sensors
		//cout << "11111111111" << endl;
		sensorRes = obstacleDetect(sensorData);
		//cout << "22222222222" << endl;
		//if (image_src == NULL)
		//	image_src = cvCreateImage(cvSize(640, 480), 8, 3);
		//if (RcvIplImage(image_src, sClient))
		//{
		//	cout << "image_src:" << image_src->imageSize << endl;
		//	cvShowImage("server", image_src);
		//}
		//else
		//	cout << "Error in recving image" << endl;

				image_src = RcvIplImage(sClient);
				if (image_src != NULL){
					cout << "image_src:" << image_src->imageSize << endl;
					cvShowImage("server", image_src);
				}
				if (cvWaitKey(1) == 'q')
					break;

		send2App(sensorRes, cameraRes);
	}


	cvReleaseImage(&image_src);
	delete[] recvData;
	delete[] sensorData;

	closesocket(slisten);
	WSACleanup();
	return 0;
}

// Receive length-byte data from buffer
bool receivedata(char* buf, int length, SOCKET s)
{
	int ret, lefti;
	lefti = length;
	while (lefti > 0)
	{
		//cout << "1111111111111" << endl;
		ret = recv(s, (char*)buf, lefti, 0);
		//cout << "ret:" << ret << endl;
		/*if (ret == -1)
		{
		int id = WSAGetLastError();
		switch (id)
		{
		case WSANOTINITIALISED: printf("not initialized\n"); break;
		case WSASYSNOTREADY: printf("sub sys not ready\n"); break;
		case WSAHOST_NOT_FOUND: printf("name server not found\n");  break;
		case WSATRY_AGAIN:  printf("server fail\n");  break;
		case WSANO_RECOVERY:  printf("no recovery\n");   break;
		case WSAEINPROGRESS:  printf("socket blocked by other prog\n"); break;
		case WSANO_DATA:   printf("no data record\n");  break;
		case WSAEINTR:   printf("blocking call canciled\n");  break;
		case WSAEPROCLIM: printf("limit exceeded\n");
		case WSAEFAULT:  printf("lpWSAData in startup not valid\n");
		default: printf("unknown error id = %d\n", id); break;
		};
		break;
		}*/
		buf += ret;
		lefti = lefti - ret;
	}
	return true;
}

IplImage* RcvIplImage(SOCKET s)//returns a pointer to the iplimage
{
	IplImage header;
	IplImage* source;
	char *databuf;
	char *index;
	int ret;
	hdr_data datainfo;
	char buf[BUFFER_SIZE + 5];
	char sync[2] = { 0, 0 };
	ret = recv(s, (char *)&header, HDRLEN_IPLIMAGE, 0);//----1 receive header
	source = cvCreateImageHeader(cvSize(640, 480), 8, 3);
	if (ret != HDRLEN_IPLIMAGE){
		return NULL;
	}
	databuf = new char[source->imageSize];
	source->imageData = databuf;
	source->imageDataOrigin = databuf;
	index = databuf;

	ret = recv(s, (char *)&datainfo, HDRLEN_DATA, 0);
	if (ret != HDRLEN_DATA){
		cout << "wrong" << endl;
		return NULL;
	}
	while (ret > 0 && datainfo.data_len != -1) {
		receivedata(buf, datainfo.data_len, s);
		memcpy(index, buf, datainfo.data_len);
		index += datainfo.data_len;
		receivedata((char *)&datainfo, HDRLEN_DATA, s);
	} 
	cout << "------------9 -----------------"<< endl;
	return source;
}

//
//// Returns a pointer to the iplimage
//bool RcvIplImage(IplImage* dst, SOCKET s)
//{
//
//		IplImage header;
//		IplImage* source;
//		char *databuf;
//		char *index;
//		int ret;
//		hdr_data datainfo;
//		char buf[BUFFER_SIZE + 5];
//		char sync[2] = { 0, 0 };
//		ret = recv(s, (char *)&header, HDRLEN_IPLIMAGE, 0);//----1 receive header
//		source = cvCreateImageHeader(cvSize(640, 480), 8, 3);
//		if (ret != HDRLEN_IPLIMAGE){
//			return NULL;
//		}
//		databuf = new char[source->imageSize];
//		source->imageData = databuf;
//		source->imageDataOrigin = databuf;
//		index = databuf;
//	
//		ret = recv(s, (char *)&datainfo, HDRLEN_DATA, 0);
//		if (ret != HDRLEN_DATA){
//			cout << "wrong" << endl;
//			return NULL;
//		}
//		while (ret > 0 && datainfo.data_len != -1) {
//			receivedata(buf, datainfo.data_len, s);
//			memcpy(index, buf, datainfo.data_len);
//			index += datainfo.data_len;
//			receivedata((char *)&datainfo, HDRLEN_DATA, s);
//		}
//		cout << "------------9 -----------------"<< endl;
//
//	//// Is error occurs in receiving header
//	//bool errorHeader = false;
//	//bool errorInfo = false;
//	//IplImage header;
//	//IplImage* source = NULL;
//	//char* databuf;
//	//char* index;
//	//int ret;
//	//struct hdr_data datainfo;
//	////char recv_buf[HDRLEN_DATA];
//	//char buf[BUFFER_SIZE + 5];
//	//char sync[2] = { 0, 0 };
//	//ret = recv(s, (char*)&header, HDRLEN_IPLIMAGE, 0);
//	//source = cvCreateImageHeader(cvSize(640, 480), 8, 3);
//	//if (ret != HDRLEN_IPLIMAGE){
//	//	cout << "Error in recv header: " << ret << endl;
//	//	errorHeader = true;
//	//	return NULL;
//	//}
//
//	//databuf = new char[source->imageSize];
//	//source->imageData = databuf;
//	//source->imageDataOrigin = databuf;
//	//index = databuf;
//
//	//ret = recv(s, (char*)&datainfo, HDRLEN_DATA, 0);
//	//if (ret != HDRLEN_DATA){
//	//	cout << "Error in recv datainfo: " << ret << endl;
//	//	errorInfo = true;
//	//	//return NULL;
//	//}
//
//	//while (ret > 0 && datainfo.data_len != -1) {
//	//	//cout << "len:"<<datainfo.data_len << endl;
//	//	receivedata(buf, datainfo.data_len, s);
//	//	//cout << "test1" << endl;
//	//	memcpy(index, buf, datainfo.data_len);
//	//	index += datainfo.data_len;
//	//	receivedata((char*)&datainfo, HDRLEN_DATA, s);
//	//	//cout << "test2" << endl;
//	//}
//	cout << "recv!" << endl;
//	cvCopy(source, dst, NULL);
//	// Release
//	cvReleaseImageHeader(&source);
//	delete[] databuf;
//	//return source;
//	cout << "size:" << dst->imageSize << endl;
//	// Return
//	if (dst->imageSize != 0 /*&& !errorHeader && !errorInfo*/)
//		return 1;
//	else
//		return 0;
//}


void dataToDecimal(char* src, int* dst)
{
	for (int i = 0; i < SENSOR_NUM; i++)
	{
		int l = (int)src[i * 2];
		int h = (int)(src[i * 2 + 1] << 8);
		dst[i] = h + l;
	}
}

//int min(int a, int b)
//{
//	if (a >= b)
//		return b;
//	else
//		return a;
//}

/*******************************
* data：8个传感器的数据
* [0]: 头部朝前
* [1]: 头部朝下
* [2]: 左肩
* [3]: 右肩
* [4]: (无用)
* [5]: 腰部朝下，37度（右，下台阶）
* [6]: 腰部朝上，75度（左）
* [7]: 左脚
* [8]: 右脚
*
* feedback：反馈探测的结果
* -1 -> NULL
* 1 -> 前方障碍物
* 2 -> 左侧障碍物
* 3 -> 右侧障碍物
* 5 -> 向上台阶
* 6 -> 向下台阶
* 7 -> 胸前有障碍物
* 8 -> 腰前有障碍物
* 9 -> 膝盖前有障碍物
* 0 -> 台阶结束
*******************************/
int obstacleDetect(int* data)
{
	state = -1;
	int distance;

	if (isBegining)
	{
		beginCount++;
		if (beginCount > 5)
		{
			//height = (int)((double)data[1] * cos((double)ANGLE_HEAD / (double)180 * PI));
			height = data[1];
			isBegining = false;
			beginCount = 0;
		}
	}
	else
	{
		distance = data[2];
		//if (distance != 0)
		state = leftDetect(distance);

		distance = data[3];
		//if (distance != 0)
		state = rightDetect(distance);

		distance = data[0];
		if (distance != 0)
			state = frontDetect(distance);

		distance = data[6];
		//if (distance != 0)
		//{
		int headFront = data[0];
		int headDown = data[1];
		state = kneeDetect(distance, headFront, headDown);
		state = waistDetect(distance, headFront, headDown);
		state = chestDetect(distance, headFront, headDown);
		//}

		distance = data[1];
		if (distance != 0)
		{
			int frontDis = min(data[7], data[8]);
			state = upstairDetect(distance, frontDis);
		}

		distance = data[1];
		if (distance != 0)
			state = downstairDetect(distance);
	}
	return state;
}

int leftDetect(int distance)
{
	int feedback = state;
	if (distance < 25)
	{
		leftCount++;
		if (distance == 0)
			leftCount = 0;
		if (leftCount > 9)
		{
			feedback = 2;
			leftCount = 0;
		}
	}
	return feedback;
}

int rightDetect(int distance)
{
	int feedback = state;
	if (distance < 25)
	{
		rightCount++;
		if (distance == 0)
			rightCount = 0;
		if (rightCount > 9)
		{
			feedback = 3;
			rightCount = 0;
		}
	}
	return feedback;
}

int frontDetect(int distance)
{
	int feedback = state;
	if (distance < 85)
	{
		frontCount++;
		if (frontCount > 8)
		{
			feedback = 1;
			frontCount = 0;
		}
	}
	return feedback;
}

int upstairDetect(int distance, int front)
{
	int feedback = state;
	//int dHeight = (int)((double)distance * cos((double)ANGLE_HEAD / (double)180 * PI));
	int dHeight = distance;
	int h;
	int times;
	int gHeight = height - dHeight;
	if (isFirstUp)
	{
		h = 30;
		times = 1;
	}
	else
	{
		h = 50;
		times = 9;
	}
	if (gHeight >= 10 && gHeight < h && front < 115)
	{
		upCount1++;
		if (upCount1 > times)
		{
			if (isFirstUp)
				isFirstUp = false;
			feedback = 5;
			beginUpstair = true;
			clearCount();
		}
	}
	else
	{
		if (abs(gHeight) < 10)
		{
			upCount2++;
			if (upCount2 >= 2)
			{
				if (beginUpstair == true)
				{
					feedback = 0;
					beginUpstair = false;
					isFirstUp = true;
					clearCount();
				}

			}
		}
	}
	return feedback;
}

int downstairDetect(int distance)
{
	int feedback = state;
	//int dHeight = (int)((double)distance * cos((double)ANGLE_HEAD / (double)180 * PI));
	int dHeight = distance;
	int gHeight = dHeight - height;

	int times;
	if (isFirstDown)
	{
		times = 1;
	}
	else
	{
		times = 9;
	}

	if (gHeight >= 6)
	{
		downCount1++;
		if (downCount1 > times)
		{
			if (isFirstDown)
				isFirstDown = false;
			feedback = 6;
			beginDownstair = true;
			clearCount();
		}
	}
	else
	{
		if (abs(gHeight) < 6)
		{
			downCount2++;
			if (downCount2 >= 3)
			{
				if (beginDownstair == true)
				{
					feedback = 0;
					beginDownstair = false;
					isFirstDown = true;
					clearCount();
				}
			}
		}
	}
	return feedback;
}

int chestDetect(int distance, int headFront, int headDown)
{
	int feedback = state;

	//int dHeight = (int)((double)distance * cos((double)ANGLE_WAIST1 / (double)180 * PI));
	//int gHeight = dHeight + WAIST_HEIGHT;
	//int d = (int)((double)distance * sin((double)ANGLE_WAIST1 / (double)180 * PI));
	//if ((d < 50 && gHeight < height && headFront > 50) || headDown < height - WAIST_HEIGHT - 10)
	//if (headDown < height - WAIST_HEIGHT - 10)
	int g = height - headDown;
	if (g >= 0.68 * height && g < 0.9 * height)
	{
		chestCount++;
		if (chestCount > 1)
		{
			feedback = 7;
			clearCount();
			frontCount = 0;
		}
	}
	return feedback;
}

int waistDetect(int distance, int headFront, int headDown)
{
	int feedback = state;
	int g = height - headDown;
	//if ((distance < 40 && headFront > 50) || (headDown >= height - WAIST_HEIGHT - 10 && headDown < height - WAIST_HEIGHT + 30))
	//if (headDown >= height - WAIST_HEIGHT - 10 && headDown < height - WAIST_HEIGHT + 30)
	if (g >= 0.4 * height && g < 0.68 * height)
	{
		waistCount++;
		if (waistCount > 1)
		{
			feedback = 8;
			clearCount();
			frontCount = 0;
		}
	}
	return feedback;
}

int kneeDetect(int distance, int headFront, int headDown)
{
	int feedback = state;
	//int d = (int)((double)distance * sin((double)ANGLE_WAIST2 / (double)180 * PI));
	int g = height - headDown;
	//if (d <= 50 && headFront > 50 || (headDown >= height - WAIST_HEIGHT + 30 && headDown < height - 30))
	//if (headDown >= height - WAIST_HEIGHT + 30 && headDown < height - 30)
	if (g >= 30 && g < 0.4 * height)
	{
		kneeCount++;
		if (kneeCount > 1 && isFirstUp)
		{
			feedback = 9;
			clearCount();
			frontCount = 0;
		}
	}
	return feedback;
}

// Set cameraRes 1 if GREEN, 0 if RED
void trafficLightHandler(IplImage* src)
{
	int redCount = 0;
	int greenCount = 0;

	Mat frame;
	Mat img;
	Mat imgYCBCR;
	Mat imgGreen;
	Mat imgRed;

	// Brightness parameter
	double a = 0.6;//0.25;
	double b = (1 - a) * 125;

	// IplImage -> Mat
	frame = cvarrToMat(src, false);
	// Reduce brightness
	frame.convertTo(img, img.type(), a, b);
	// Convert to YCrCb color space
	cvtColor(img, imgYCBCR, CV_BGR2YCrCb);

	// Initial mat
	imgRed.create(imgYCBCR.rows, imgYCBCR.cols, CV_8UC1);
	imgGreen.create(imgYCBCR.rows, imgYCBCR.cols, CV_8UC1);

	// Sepatare color components
	vector<Mat> planes;
	split(imgYCBCR, planes);
	// Iterators
	MatIterator_<uchar> it_Cr = planes[1].begin<uchar>(),
		it_Cr_end = planes[1].end<uchar>();
	MatIterator_<uchar> it_Red = imgRed.begin<uchar>();
	MatIterator_<uchar> it_Green = imgGreen.begin<uchar>();
	// Traversing image, 
	for (; it_Cr != it_Cr_end; ++it_Cr, ++it_Red, ++it_Green)
	{
		// Red: 145<Cr<470 
		if (*it_Cr > 145 && *it_Cr < 470)
			*it_Red = 255;
		else
			*it_Red = 0;

		// Green: 95<Cr<110
		if (*it_Cr > 95 && *it_Cr < 110)
			*it_Green = 255;
		else
			*it_Green = 0;
	}

	// Dilate and erode
	dilate(imgRed, imgRed, Mat(15, 15, CV_8UC1), Point(-1, -1));
	erode(imgRed, imgRed, Mat(1, 1, CV_8UC1), Point(-1, -1));
	dilate(imgGreen, imgGreen, Mat(15, 15, CV_8UC1), Point(-1, -1));
	erode(imgGreen, imgGreen, Mat(1, 1, CV_8UC1), Point(-1, -1));

	redCount = processImgR(imgRed);
	greenCount = processImgG(imgGreen);

	//if (greenCount > 0)
	//	cross(true);
	//else
	if (redCount > 25000){
		cross(false);
		cout << "DETECT ---------------RED" << endl;
	}
		
	else if (greenCount > 25000){
		cross(true);
		cout << "DETECT ---------------GREEN" << endl;
	}
		

		if (canPass){
			cameraRes = 1;
			cout << "GREEN------------------------" << endl;
		}
		
		else{
			cameraRes = 0;
			cout << "------------------------RED" << endl;
		}

}

int processImgR(Mat src)
{
	Mat tmp;

	vector<vector<Point> > contours;
	vector<Vec4i> hierarchy;
	vector< Point > hull;	// Point set of convex

	CvPoint2D32f tempNode;
	CvMemStorage* storage = cvCreateMemStorage();
	CvSeq* pointSeq = cvCreateSeq(CV_32FC2, sizeof(CvSeq), sizeof(CvPoint2D32f), storage);

	Rect* trackBox;
	Rect* result;
	int resultNum = 0;

	int area = 0;

	src.copyTo(tmp);
	// Extract contour 
	findContours(tmp, contours, hierarchy, CV_RETR_CCOMP, CV_CHAIN_APPROX_SIMPLE);

	if (contours.size() > 0)
	{
		trackBox = new Rect[contours.size()];
		result = new Rect[contours.size()];

		// Determine the tracking area
		for (int i = 0; i < contours.size(); i++)
		{
			cvClearSeq(pointSeq);
			// Obtain the point set of convex
			convexHull(Mat(contours[i]), hull, true);
			int hullcount = (int)hull.size();
			// Save the points of convex
			for (int j = 0; j < hullcount - 1; j++)
			{
				tempNode.x = hull[j].x;
				tempNode.y = hull[j].y;
				cvSeqPush(pointSeq, &tempNode);
			}

			// Get the smallest rectangle that encloses the connected region
			trackBox[i] = cvBoundingRect(pointSeq);
		}

		if (isFirstDetectedR)
		{
			lastTrackBoxR = new Rect[contours.size()];
			for (int i = 0; i < contours.size(); i++)
				lastTrackBoxR[i] = trackBox[i];
			lastTrackNumR = contours.size();
			isFirstDetectedR = false;
		}
		else
		{
			for (int i = 0; i < contours.size(); i++)
			{
				for (int j = 0; j < lastTrackNumR; j++)
				{
					if (isIntersected(trackBox[i], lastTrackBoxR[j]))
					{
						result[resultNum] = trackBox[i];
						break;
					}
				}
				resultNum++;
			}
			delete[] lastTrackBoxR;
			lastTrackBoxR = new Rect[contours.size()];
			for (int i = 0; i < contours.size(); i++)
			{
				lastTrackBoxR[i] = trackBox[i];
			}
			lastTrackNumR = contours.size();
		}

		delete[] trackBox;
	}
	else
	{
		isFirstDetectedR = true;
		result = NULL;
	}
	cvReleaseMemStorage(&storage);

	if (result != NULL)
	{
		for (int i = 0; i < resultNum; i++)
		{
			area += result[i].area();
		}
	}
	delete[] result;

	return area;
}

int processImgG(Mat src)
{
	Mat tmp;

	vector<vector<Point> > contours;
	vector<Vec4i> hierarchy;
	vector< Point > hull;	// Point set of convex

	CvPoint2D32f tempNode;
	CvMemStorage* storage = cvCreateMemStorage();
	CvSeq* pointSeq = cvCreateSeq(CV_32FC2, sizeof(CvSeq), sizeof(CvPoint2D32f), storage);

	Rect* trackBox;
	Rect* result;
	int resultNum = 0;

	int area = 0;

	src.copyTo(tmp);
	// Extract contour 
	findContours(tmp, contours, hierarchy, CV_RETR_CCOMP, CV_CHAIN_APPROX_SIMPLE);

	if (contours.size() > 0)
	{
		trackBox = new Rect[contours.size()];
		result = new Rect[contours.size()];

		// Determine the tracking area
		for (int i = 0; i < contours.size(); i++)
		{
			cvClearSeq(pointSeq);
			// Obtain the point set of convex
			convexHull(Mat(contours[i]), hull, true);
			int hullcount = (int)hull.size();
			// Save the points of convex
			for (int j = 0; j < hullcount - 1; j++)
			{
				//line(showImg, hull[j + 1], hull[j], Scalar(255, 0, 0), 2, CV_AA);
				tempNode.x = hull[j].x;
				tempNode.y = hull[j].y;
				cvSeqPush(pointSeq, &tempNode);
			}

			// Get the smallest rectangle that encloses the connected region
			trackBox[i] = cvBoundingRect(pointSeq);
		}

		if (isFirstDetectedG)
		{
			lastTrackBoxG = new Rect[contours.size()];
			for (int i = 0; i < contours.size(); i++)
				lastTrackBoxG[i] = trackBox[i];
			lastTrackNumG = contours.size();
			isFirstDetectedG = false;
		}
		else
		{
			for (int i = 0; i < contours.size(); i++)
			{
				for (int j = 0; j < lastTrackNumG; j++)
				{
					if (isIntersected(trackBox[i], lastTrackBoxG[j]))
					{
						result[resultNum] = trackBox[i];
						break;
					}
				}
				resultNum++;
			}
			delete[] lastTrackBoxG;
			lastTrackBoxG = new Rect[contours.size()];
			for (int i = 0; i < contours.size(); i++)
			{
				lastTrackBoxG[i] = trackBox[i];
			}
			lastTrackNumG = contours.size();
		}

		delete[] trackBox;
	}
	else
	{
		isFirstDetectedG = true;
		result = NULL;
	}
	cvReleaseMemStorage(&storage);

	if (result != NULL)
	{
		for (int i = 0; i < resultNum; i++)
		{
			area += result[i].area();
		}
	}
	delete[] result;

	return area;
}

// Determines whether the two rectangles intersect
bool isIntersected(Rect r1, Rect r2)
{
	int minX = max(r1.x, r2.x);
	int minY = max(r1.y, r2.y);
	int maxX = min(r1.x + r1.width, r2.x + r2.width);
	int maxY = min(r1.y + r1.height, r2.y + r2.height);

	if (minX < maxX && minY < maxY)
		return true;
	else
		return false;
}

void cross(bool isRed)
{
	// initial
	if (firstSignal == -1)
	{
		if (isRed)
			firstSignal = 0;
		else
			firstSignal = 1;
	}
	// the first is red
	else if (firstSignal == 0)
	{
		if (isRed)
			canPass = true;
		else
			canPass = false;
	}
	// the first is green
	else
	{
		if (!isRed)
		{
			hasRed = true;
			canPass = false;
		}
		else
		{
			if (hasRed)
				canPass = true;
			else
				canPass = false;
		}
	}
}

// Determines whether to cross or not
//void cross(bool isGreen)
//{
//	// initial
//	if (firstSignal == -1)
//	{
//		if (isGreen)
//			firstSignal = 1;
//		else
//			firstSignal = 0;
//	}
//	// the first is red
//	else if (firstSignal == 0)
//	{
//		if (isGreen)
//			canPass = true;
//		else
//			canPass = false;
//	}
//	// the first is green
//	else
//	{
//		if (!isGreen)
//		{
//			hasRed = true;
//			canPass = false;
//		}
//		else
//		{
//			if (hasRed)
//				canPass = true;
//			else
//				canPass = false;
//		}
//	}
//}

// Determines whether is deviated from the road
void roadHandler(IplImage* frame)
{
	nFrmNum++;

	if (nFrmNum % 3 == 0)
	{
		src = cvarrToMat(frame);
		//imshow("src", src);

		colorReduce(src, ttt);

		int width = src.cols;
		int height = src.rows;
		int cutWidth = 300;
		IplConvKernel* kenel1 = cvCreateStructuringElementEx(20, 20, 10, 10, CV_SHAPE_RECT, NULL);
		IplConvKernel* kenel2 = cvCreateStructuringElementEx(3, 3, 1, 1, CV_SHAPE_RECT, NULL);
		IplConvKernel* kenel3 = cvCreateStructuringElementEx(1, 3, 0, 1, CV_SHAPE_RECT, NULL);
		IplConvKernel* kenel4 = cvCreateStructuringElementEx(11, 11, 5, 5, CV_SHAPE_RECT, NULL);

		Rect rect(cutWidth, 0, width - cutWidth, height);
		roi = src(rect);

		Mat grad_x, grad_y, abs_grad_x, abs_grad_y;
		cvtColor(ttt, src_gray, COLOR_BGR2GRAY);
		IplImage temp = (IplImage)src_gray;
		IplImage* tempImg = &temp;
		cvDilate(tempImg, tempImg, kenel2, 1);
		cvErode(tempImg, tempImg, kenel1, 1);
		cvDilate(tempImg, tempImg, kenel1, 1);

		Canny(cvarrToMat(tempImg), dst, 100, 300);

		Rect rect1(width / 2 + 50, height / 3, width / 2 - 50, height * 2 / 3);
		roi = dst(rect1);

		temp = (IplImage)roi;
		tempImg = &temp;
		cvDilate(tempImg, tempImg, kenel4, 1);


		CvMemStorage* storage = cvCreateMemStorage();
		CvSeq* contour = NULL;

		cvFindContours(tempImg, storage, &contour, sizeof(CvContour), CV_RETR_CCOMP, CV_CHAIN_APPROX_SIMPLE);

		int max = 0;
		CvRect rrr;

		while (contour != NULL)
		{
			CvRect contourRect = cvBoundingRect(contour, 0);
			if (contourRect.width * contourRect.height > max && contourRect.width < 160
				&& contourRect.width > 30 && contourRect.height > 30)
			{
				max = contourRect.width * contourRect.height;
				rrr = contourRect;
			}
			contour = contour->h_next;
		}

		//if ((rrr.width > 20) && (rrr.height >50))
		{  //6

			cvRectangle(tempImg, cvPoint(rrr.x, rrr.y),
				cvPoint(rrr.x + rrr.width, rrr.y + rrr.height), CV_RGB(255, 255, 255), 4, 8, 0);
		}

		int x1 = rrr.x + width / 2 + 50;
		int y1 = rrr.y + height / 3;
		int x2 = rrr.x + rrr.width + width / 2 + 50;
		int y2 = rrr.y + rrr.height + height / 3;
		int h = 480;
		int w = 640;
		line(src, cvPoint(rightEdge, 0),
			cvPoint(rightEdge, 476),
			CV_RGB(255, 0, 0), 4, 8, 0);
		int x = -1;
		if (x1 != x2&&y1 != y2)
		{
			x = (double)(((h - y1)*(x2 - x1)) / (y2 - y1)) + x1 - 25;
			if (x < 640)
			{
				circle(src, cvPoint(x, 476), 3, CV_RGB(0, 255, 0), 3, 8, 3);
				line(src, cvPoint(x, 0),
					cvPoint(x, 476),
					CV_RGB(255, 0, 0), 4, 8, 0);
			}
			else
			{
				int y = (double)(((y2 - y1)*(w - x1)) / (x2 - x1)) + y1;
				circle(src, cvPoint(636, y), 3, CV_RGB(0, 255, 0), 3, 8, 3);
				line(src, cvPoint(0, y),
					cvPoint(w, y),
					CV_RGB(255, 0, 0), 4, 8, 0);
			}
			line(src, cvPoint(x1, y1),
				cvPoint(x2, y2),
				CV_RGB(255, 255, 255), 4, 8, 0);
		}
		if (x > 0)
		{
			if (edgeTimes == 0)
			{
				if (x > rightEdge)
				{
					++edgeTimes;
				}
			}
			else
			{
				if (x > rightEdge)
				{
					++edgeTimes;
					if (edgeTimes >= times)
					{
						cameraRes = 2;
						edgeTimes = 0;
					}

				}
				else
				{
					--edgeTimes;
				}
			}

			//imshow("Detect", src);
		}

		cvReleaseMemStorage(&storage);
		cvReleaseStructuringElement(&kenel1);
		cvReleaseStructuringElement(&kenel2);
		cvReleaseStructuringElement(&kenel3);
		cvReleaseStructuringElement(&kenel4);
	}
}

bool detectPix(Vec3b& target, const Vec3b& origin)
{
	if (abs(target[0] - origin[0]) < 40 &&
		abs(target[1] - origin[1]) < 60 &&
		abs(target[2] - origin[2]) < 60)
		return true;
	return false;
}

void colorReduce(const Mat& image, Mat& outImage)
{

	int nr = image.rows;
	int nc = image.cols;

	Vec3b ori = image.at<Vec3b>(nr / 3, nc * 2 / 3);
	outImage.create(image.size(), image.type());
	if (image.isContinuous() && outImage.isContinuous())
	{
		nr = 1;
		nc = nc*image.rows*image.channels();
	}
	for (int i = 0; i<nr; i++)
	{
		const uchar* inData = image.ptr<uchar>(i);
		uchar* outData = outImage.ptr<uchar>(i);
		for (int j = 0; j<nc / 3; j++)
		{
			Vec3b c = Vec3b(*inData++, *inData++, *inData++);
			if (detectPix(c, ori)){
				*outData++ = 0;
				*outData++ = 0;
				*outData++ = 0;
			}
			else{
				*outData++ = 255;
				*outData++ = 255;
				*outData++ = 255;
			}
		}
	}
}

// Function of imgHandlerThread
void* imgThreadFunc(void* arg)
{
	//slisten = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
	//SOCKET cSocket = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
	//WSADATA wsaData;
	//int nRet;
	//if ((nRet = WSAStartup(MAKEWORD(2, 2), &wsaData)) != 0){
	//	//TRACE("WSAStartup failed");
	//	exit(0);
	//}
	//需要绑定的参数








	//int opt_client = 1;
	//if ((setsockopt(client_sockfd_app, SOL_SOCKET, SO_REUSEADDR, &opt_client, sizeof(opt_client))) < 0)
	//{
	//	perror("setsockopt");
	//	exit(EXIT_FAILURE);
	//}

	//// Bind,return 0 if success, else -1
	//if (bind(client_sockfd_app, (struct sockaddr*) &client_sockaddr_app, sizeof(client_sockaddr_app)) == -1)
	//{
	//	perror("bind");
	//	exit(1);
	//}

	//// Listen，return 0 if success, else -1
	//if (listen(client_sockfd_app, QUEUE1) == -1)
	//{
	//	perror("listen");
	//	exit(1);
	//}

	// Client socket
	//char buffer1_client[BUFFER_SIZE];
	//struct sockaddr_in client_addr_app1;
	//socklen_t length_app_client = sizeof(client_addr_app1);

	//// Connect，return an non-negative descriptor if success, else -1
	//cout << "Waiting for connection to App..." << endl;
	//conn_app_client = accept(client_sockfd_app, (struct sockaddr*) &client_addr_app1, &length_app_client);
	//if (conn_app_client < 0)
	//{
	//	perror("connect");
	//	exit(1);
	//}
	//cout << "Connection to app accepted!" << endl;





	while (true)
	{
		cout << "thread---------------------" << endl;
		int len = recv(conn_app_server,buffer1, strlen(buffer1) + 1, 0);
		cout << "QQQQQQQQQQQQQQQQQQQQ" << endl;
		if (len > 0)
		{
			if (buffer1[0] == '1')
			{
				isRoad = true;
				cout << "road" << endl;
			}
			else
			{
				isRoad = false;
				cout << "light" << endl;
			}
		}

		if (isRoad)
		{
			roadHandler(image_src);
		}
		else
		{
			//trafficLightHandler(image_src);
		}
	}
}

// Send result to app
void send2App(int sd, int cd)
{
	string sensorStr, cameraStr;
	ss.clear();
	ss << sd;
	ss >> sensorStr;
	ss.clear();
	ss << cd;
	ss >> cameraStr;
	string commaStr = ",";
	string temp = sensorStr + commaStr + cameraStr + "\n";
	char* c;
	const int len = temp.length();
	c = new char[len + 1];
	strcpy(c, temp.c_str());
	int ret = send(conn_app_client, c, len, 0);
	if (sd != -1)
		cout << "===========" << "height:" << height << "," << c << "===========" << endl;
	if (cd != -1 && cd == cameraRes)
		cameraRes = -1;
}

void clearCount()
{
	upCount1 = 0;
	upCount2 = 0;
	chestCount = 0;
	waistCount = 0;
	kneeCount = 0;
	downCount1 = 0;
	downCount2 = 0;
}
