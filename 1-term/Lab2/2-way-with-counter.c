#include <stdio.h>
#include <math.h>
#include <conio.h>


int main() {
    int n;
    int oper_counter = 0;
    int trig_counter = 0;
    double sum = 0.0;
    double mult = 1.0;
    printf("Enter n:");
    scanf("%d", &n);
    oper_counter += 4;
 
 
    for (int i = 1; i <= n; i++) {
        mult *= i - sin(i);
        double buff = (i + cos(i)) / mult;
        sum += buff;
        oper_counter += 9;
        trig_counter += 2;
    }
    int counter_sum = oper_counter + trig_counter;
    printf("Result is %.7lf\n", sum);
    printf("Operations counter is %u\n", counter_sum);
   _getch();
    return 0;
}
