#ifndef __MY_HELPER__
#define __MY_HELPER__

#include <cstdio>
#include <cstring>
#include <cstdlib>
#include <cctype>
#include <cassert>
#include <algorithm>
#include <iostream>
#include <set>
#include <map>
#include <vector>
#include <string>
#include <sstream>
using namespace std;

#define FOR(i,a) for (__typeof((a).begin()) i = (a).begin(); i != (a).end(); ++ i)

bool assertTrue(bool flg, string msg)
{
	if (!flg) {
		cerr << msg << endl;
		exit(-1);
	}
}

const int maxlength = 100000000;

char line[maxlength + 1];

bool getLine(FILE* in)
{
	bool ret = fgets(line, maxlength, in);
	int len = strlen(line);
	while (len > 0 && (line[len - 1] == '\n' || line[len - 1] == '\r')) {
		line[len - 1] = 0;
		-- len;
	}
	return ret;
}

FILE* tryOpen(string filename, string param)
{
	FILE* ret = fopen(filename.c_str(), param.c_str());
	if (ret == NULL) {
		fprintf(stderr, "error while opening %s\n", filename.c_str());
	}
	return ret;
}

vector<string> splitBy(const string &line, char comma)
{
	vector<string> tokens;
	string token = "";
	bool inside = false;
	for (int i = 0; i < line.size(); ++ i) {
		if (line[i] == '"') {
			inside ^= 1;
		} else if (line[i] == comma && !inside) {
			tokens.push_back(token);
			token = "";
		} else {
			token += line[i];
		}
	}
	tokens.push_back(token);
	if (inside) {
		tokens.clear();
	}
	return tokens;
}

bool fromString(const string &s, string &x)
{
	x = s;
	return true;
}

template<class T>
bool fromString(const string &s, T &x)
{
	stringstream in(s);
	return (in >> x);
}

bool __firstToDate = true;
int __days[3001];
const int __monthDay[13] = {0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

bool isLeap(int y) 
{
	return y % 400 == 0 || y % 100 != 0 && y % 4 == 0;
}

string toData(long long timestamp, bool mills = false)
{
	if (__firstToDate) {
		__firstToDate = false;
		memset(__days, 0, sizeof(__days));
		for (int year = 1970; year <= 3000; ++ year) {
			__days[year] = __days[year - 1] + 365;
			if (isLeap(year)) {
				++ __days[year];
			}
		}
	}
	
	long long seconds = timestamp;
	if (mills) {
		seconds /= 1000;
	}
	long long days = seconds / (60 * 60 * 24);
	seconds -= days * 60 * 60 * 24;
	int year = lower_bound(__days, __days + 3001, days) - __days;
	int leftDays = days - __days[year - 1];
	int left_seconds = seconds - days * 60 * 60 * 24;
	int month = 0;
	for (int i = 1; i <= 12; ++ i) {
		int cur = __monthDay[i];
		if (i == 2 && isLeap(year)) {
			++ cur;
		}
		if (leftDays <= cur) {
			month = i;
			break;
		} else {
			leftDays -= cur;
		}
	}
	assertTrue(month > 0, "Data Format Error!");
	int hh = seconds / (60 * 60);
	int mm = (seconds - hh * 60 * 60) / 60;
	int ss = seconds % 60;
	
	char temp[100];
	sprintf(temp, "%d-%02d-%02d_%02d:%02d:%02d", year, month, leftDays, hh, mm, ss);
	
	return temp;
}

#endif
