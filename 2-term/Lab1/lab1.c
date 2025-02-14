#include <stdio.h>
#include <conio.h>

double recursiveDescent(unsigned n, double x, int i, double intermediateResult);

double recursiveAscent(unsigned n, double x, int i);

double recursiveWrapper(unsigned n, double x, int i, double prev);

double mixedRecursiveFunction(unsigned n, double x);

int main()
{
  double x;
  unsigned int n;

  printf("Enter x: ");
  scanf("%lf", &x);
  printf("Enter n: ");
  scanf("%u", &n);

  printf("Result (Recursive Descent) = %lf\n", recursiveDescent(n, x, n, 1.0));
  printf("Result (Recursive Ascent) = %lf\n", recursiveAscent(n, x, 1));
  printf("Result (Mixed Recursion) = %lf\n", mixedRecursiveFunction(n, x));

  getch();
  return 0;
}

double recursiveDescent(unsigned n, double x, int i, double intermediateResult)
{
  int startValue = 1;

  if (i == 0)
    return intermediateResult;

  intermediateResult = startValue - ((x * x) / (4.0 * i * i - 2.0 * i)) * intermediateResult;
  return recursiveDescent(n, x, i - 1, intermediateResult);
}

double recursiveAscent(unsigned n, double x, int i)
{
  int startValue = 1;

  if (i == n)
    return startValue;

  return startValue - ((x * x) / (4.0 * i * i - 2.0 * i)) * recursiveAscent(n, x, i + 1);
}

double recursiveWrapper(unsigned n, double x, int i, double intermediateResult)
{
  if (i == n)
    return intermediateResult;

  double next = -intermediateResult * (x * x) / (4.0 * i * i - 2.0 * i);
  return intermediateResult + recursiveWrapper(n, x, i + 1, next);
}

double mixedRecursiveFunction(unsigned n, double x)
{
  return recursiveWrapper(n, x, 1, 1.0);
}
