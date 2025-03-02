#include <stdio.h>
#include <conio.h>

double recursiveDescent(unsigned n, double x, int i, double current, double initSum);

double recursiveAscent(unsigned n, double x, int i);

double recursiveWrapper(unsigned n, double x, int i, double prev);

double mixedRecursiveFunction(unsigned n, double x);

double cyclicalFunction(unsigned n, double x);

int main()
{
  const double initSum = 0.0;
  double x;
  unsigned int n;

  printf("Enter x: ");
  scanf("%lf", &x);
  printf("Enter n: ");
  scanf("%u", &n);

  printf("Result (Recursive Descent) = %lf\n", recursiveDescent(n, x, 1, 1.0, initSum));
  printf("Result (Recursive Ascent) = %lf\n", recursiveAscent(n, x, 1));
  printf("Result (Mixed Recursion) = %lf\n", mixedRecursiveFunction(n, x));
  printf("Result (Cyclical Function) = %lf\n", cyclicalFunction(n, x));

  getch();
  return 0;
}

double recursiveDescent(unsigned n, double x, int i, double current, double initSum)
{
  if (i > n)
    return initSum;

  initSum += current;
  double next = -current * (x * x) / (4.0 * i * i - 2.0 * i);
  return recursiveDescent(n, x, i + 1, next, initSum);
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

  double next = -intermediateResult * ((x * x) / (4.0 * i * i - 2.0 * i));
  return intermediateResult + recursiveWrapper(n, x, i + 1, next);
}

double mixedRecursiveFunction(unsigned n, double x)
{
  return recursiveWrapper(n, x, 1, 1.0);
}

double cyclicalFunction(unsigned n, double x)
{
  double intermediateResult = 1;
  double result = intermediateResult;
  for (int i = 1; i < n; i++)
  {
    intermediateResult = -intermediateResult * ((x * x) / (4.0 * i * i - 2.0 * i));
    result += intermediateResult;
  }
  return result;
}
