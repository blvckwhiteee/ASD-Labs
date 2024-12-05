#include <stdio.h>
#include <math.h>
#include <conio.h>


int main() {
   int n;
   double res = 0.0;
   printf("Enter n: ");
   scanf("%d", &n);


   for (int i = 1; i <= n; i++) {
       double buff = 1.0;
       for (int j = 1; j <= i; j++) {
           buff *= j - sin(j);
       }
       res += (i + cos(i)) / buff;
   }
   printf("%.7lf\n", res);
   _getch();
   return 0;
}
