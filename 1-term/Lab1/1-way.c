#include <stdio.h>
#include <stdlib.h>
#include <conio.h>


int main()
{
   float x;
   float y; 
   printf("Type your X: ");
   scanf("%f",&x);
   if (x > 0)
   {
       if (x <= 5)
       {
           y = x*x*x - 5*x*x;
           printf("Result = %f",y);
       }
       else if (x > 10)
       {
           y = x*x - 3;
           printf("Result = %f",y);
       }
       else
       {
           printf("No solution :(");
       }
   }
   else if (x >= -32)
   {
       if (x < -20)
       {
           y = x*x - 3;
           printf("Result = %f",y);
       }
       else
       {
           printf("No solution :(");
       }
   }
   else
   {
       printf("No solution :(");
   }
   _getch();
   return 0;
}
