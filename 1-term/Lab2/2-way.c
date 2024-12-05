#include <stdio.h>
#include <math.h>
#include <conio.h>


int main() {
   int n;
   double sum = 0.0;
   double mult = 1.0;
   printf("Enter n: ");
   scanf("%d", &n);


   for (int i = 1; i <= n; i++) {
       mult *= i - sin(i);
       double buff = (i + cos(i)) / mult;
       sum += buff;
   }
   printf("%.7lf\n", sum);
   _getch();
   return 0;
}
