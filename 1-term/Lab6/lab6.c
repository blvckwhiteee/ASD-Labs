#include <stdio.h>
#include <conio.h>


void outputMatrix (int rows, int cols, int arr[rows][cols]);
void sortMatrix (int rows, int cols, int arr[rows][cols]);


int main () {
        const int ROWS = 10;
        const int COLS = 10;
        int arr[10][10] = {
            {1, 2, 3, 4, 5, 6, 7, 8, 9, 10},
            {10, 9, 8, 7, 6, 5, 4, 3, 2, 1},
            {3, 1, 4, 1, 5, 9, 2, 6, 5, 3},
            {11, 13, 17, 19, 23, 29, 31, 37, 41, 43},
            {50, 45, 40, 35, 30, 25, 20, 15, 10, 5},
            {7, 3, 8, 2, 10, 5, 1, 6, 9, 4},
            {11, 12, 13, 14, 15, 16, 17, 18, 19, 20},
            {20, 19, 18, 17, 16, 15, 14, 13, 12, 11},
            {-12, -8, -15, -10, -5, -18, -2, -20, -7, -3},
            {21, 22, 23, 24, 25, 26, 27, 28, 29, 30},
        };
        printf("Array before sorting:\n\n");
        outputMatrix(ROWS, COLS, arr);


        printf("\nArray after sorting:\n\n");
        sortMatrix(ROWS, COLS, arr);
     getch();
     return 0;
}


void outputMatrix (int rows, int cols, int arr[rows][cols]) {
    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols ; j++) {
            printf("%3d ", arr[i][j]);
        }
        printf("\n");
    }
}


void sortMatrix(int rows, int cols, int arr[rows][cols]) {
    for (int i = 0; i < rows; i++) {
        for (int j = cols - 2; j >= 0; j--) {
            int barrier = arr[i][j];
            int k = j + 1;
 
 
            while (k < cols && arr[i][k] > barrier) {
                arr[i][k - 1] = arr[i][k];
                k++;
            }
 
 
            arr[i][k - 1] = barrier;
        }
    }
    outputMatrix(rows, cols, arr);
}
