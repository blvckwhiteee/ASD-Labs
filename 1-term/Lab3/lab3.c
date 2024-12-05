#include <stdio.h>
#include <stdlib.h>
#include <conio.h>


int main() {
    int m, n, i, j;
    printf("Type quantity of rows and columns:");
    scanf("%d %d", &m, &n);
    float A[m][n];
    printf("-----------------------------\n");
    // Input matrix
    for (i = 0; i < m; i++) {
        for (j = 0; j < n; j++) {
            printf("Type [%d][%d] element:", i, j);
            scanf("%f", &A[i][j]);
        }
    }
    // Output matrix
    printf("-----------------------------\n");
    for (i = 0; i < m; i++) {
        for (j = 0; j < n ; j++) {
            printf("%.2f ", A[i][j]);
        }
        printf("\n");
    }
    printf("-----------------------------\n");
    // Finding the last min element
    float min = A[0][0];
    int minRow = 0, minCol = 0;
    for (i = 0; i < m; i++) {
        for (j = 0; j < n; j++) {
            if (A[i][j] <= min) {
                min = A[i][j];
                minRow = i;
                minCol = j;
            }
        }
    }
    // The last min element
    printf("The last min element is: %.2f\n", min);
    printf("Coordinates are: [%d][%d]\n", minRow, minCol);
    _getch();
    return 0;
}
