#include <stdio.h>
#include <windows.h>


int find_X (int matrix[10][10], int n, int X) {
   int L = 0;
   int R = n - 1;
   while (L <= R) {
      for (int i = 0; i < n; i++) {
         int mid = ((L + R) / 2);
         if ((matrix[mid][n-mid-1] == X) && (L == R)) {
            return mid;
         }
         else if (matrix[mid][n-mid-1] < X) {
            L = mid + 1;
         }
         else {
            R = mid;
         }
      }
   }
   return -1;
}


int main(void) {
   int n = 10;
   int matrix [10][10] = {
      {1,1,1,1,1,1,1,1,1,5},
      {1,1,1,1,1,1,1,1,6,1},
      {1,1,1,1,1,1,1,6,1,1},
      {1,1,1,1,1,1,6,1,1,1},
      {1,1,1,1,1,7,1,1,1,1},
      {1,1,1,1,7,1,1,1,1,1},
      {1,1,1,7,1,1,1,1,1,1},
      {1,1,7,1,1,1,1,1,1,1},
      {1,8,1,1,1,1,1,1,1,1},
      {8,1,1,1,1,1,1,1,1,1},
      };
   printf("Matrix is:\n");
   for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
         printf("%d ", matrix[i][j]);
      }
      printf("\n");
   }
   printf("\n----------------------------\n\n");
   int X;
   printf("Enter X:");
   scanf("%d", &X);
   printf("----------------------------\n");
   int result = find_X(matrix, n, X);
   if (result != -1) {
      printf("Secondary diagonal is: ");
      int position = n - result - 1;
      printf("\nThe X was found on [%d][%d] position",  result, position);
   }
   else {
      printf("The X was not found! :(");
   }
   Sleep(10000);
   return 0;
}

// {
//    {
//       {1,1,1,4},
//      {1,1,4,1},
//      {1,5,1,1},
//      {6,1,1,1}
//    }
// }
// {
// {0, 0, 0, 0, 0, 0, 1},
// {0, 0, 0, 0, 0, 1, 0},
// {0, 0, 0, 0, 1, 0, 0},
// {0, 0, 0, 2, 0, 0, 0},
// {0, 0, 2, 0, 0, 0, 0},
// {0, 2, 0, 0, 0, 0, 0},
// {3, 0, 0, 0, 0, 0, 0},
// }
// {
//    {
//       {1,1,1,1,1,1,1,1,1,5},
//       {1,1,1,1,1,1,1,1,6,1},
//       {1,1,1,1,1,1,1,6,1,1},
//       {1,1,1,1,1,1,6,1,1,1},
//       {1,1,1,1,1,7,1,1,1,1},
//       {1,1,1,1,7,1,1,1,1,1},
//       {1,1,1,7,1,1,1,1,1,1},
//       {1,1,7,1,1,1,1,1,1,1},
//       {1,8,1,1,1,1,1,1,1,1},
//       {8,1,1,1,1,1,1,1,1,1},
//       }
// }
