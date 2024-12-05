#include <stdio.h>
#include <math.h>
#include <conio.h>


int main() {
    int n;
    int oper_counter = 0;
    int sin_counter = 0;
    int cos_counter = 0;
    double res = 0.0;
    printf("Enter n:");
    scanf("%d", &n);
    oper_counter += 3;


    for (int i = 1; i <= n; i++) {
        double buff = 1.0;
        for (int j = 1; j <= i; j++) {
            buff *= j - sin(j);
            oper_counter += 5;
            sin_counter += 1;
        }
        res += (i + cos(i)) / buff;
        oper_counter += 8;
        cos_counter += 1;
    }
    int oper_sum = oper_counter + sin_counter + cos_counter;
    printf("Result is %.7lf\n", res);
    printf("Operations counter is %u\n", oper_sum);
    _getch();
    return 0;
}
