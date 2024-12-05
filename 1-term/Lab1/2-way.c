#include <stdio.h>
#include <conio.h>


int main()
{
   float x;
   float y;
   printf("Type your X: ");
   scanf("%f",&x);
   if ( 0 < x && x <= 5)
   {
       y = x*x*x - 5*x*x;
       printf("Result = %f",y);
   }
   else
       if (-32 <= x && x < -20 || x > 10)
       {
           y = x*x - 3;
           printf("Result = %f",y);
       }
       else
       {
           printf("No solution :(");
       }
    _getch();
    return 0;
}
