#include <stdio.h>
#include <stdlib.h>

typedef struct Node
{
  int value;
  struct Node *next, *prev;
} Node;

int calculateExpression(Node *head);

Node *createNode(int value);

void addNode(Node **head, int value);

void deleteNode(Node **head, int value);

void printList(Node *head);

void clearList(Node **head);

int main()
{
  Node *head = NULL;
  int size;
  printf("Enter size of the list: ");
  if (scanf("%d", &size) != 1 || size < 1)
  {
    fprintf(stderr, "Invalid size.\n");
    return EXIT_FAILURE;
  }

  for (int i = 0; i < size; i++)
  {
    int value;
    printf("Enter element: ");
    scanf("%d", &value);
    addNode(&head, value);
  }
  printList(head);
  printf("Result: %d\n", calculateExpression(head));

  clearList(&head);
  return 0;
}

int calculateExpression(Node *head)
{
  if (!head)
    return 0;

  int size = 1;
  Node *temp = head->next;
  while (temp != head)
  {
    size++;
    temp = temp->next;
  }

  Node *current = head;
  Node *tail = head->prev;

  if (size == 1)
    return current->value;
  int result = 1;

  for (int i = size; i >= 2; i--)
  {
    int a_i = current->value;
    int a_next = current->next->value;
    int a_tail = tail->value;
    int term = a_i + a_next + (2 * a_tail);
    result *= term;
    current = current->next;
    tail = tail->prev;
  }
  return result;
}

Node *createNode(int value)
{
  Node *newNode = malloc(sizeof(Node));
  if (!newNode)
  {
    fprintf(stderr, "Memory allocate error\n");
    exit(EXIT_FAILURE);
  }
  newNode->value = value;
  newNode->next = newNode->prev = newNode;
  return newNode;
}

void addNode(Node **head, int value)
{
  Node *newNode = createNode(value);
  if (!*head)
  {
    *head = newNode;
    return;
  }
  Node *tail = (*head)->prev;
  tail->next = newNode;
  newNode->prev = tail;
  newNode->next = *head;
  (*head)->prev = newNode;
}

void deleteNode(Node **head, int value)
{
  if (!head)
  {
    fprintf(stderr, "List is empty.\n");
    exit(EXIT_FAILURE);
  }

  Node *curr = *head, *toDelete = NULL;
  do
  {
    if (curr->value == value)
    {
      toDelete = curr;
      break;
    }
    curr = curr->next;
  } while (curr != *head);

  if (!toDelete)
    return;

  if (toDelete->next == toDelete)
  {
    free(toDelete);
    *head = NULL;
    return;
  }

  if (toDelete == *head)
    *head = toDelete->next;

  toDelete->prev->next = toDelete->next;
  toDelete->next->prev = toDelete->prev;
  free(toDelete);
}

void printList(Node *head)
{
  if (!head)
  {
    fprintf(stderr, "List is empty.\n");
    exit(EXIT_FAILURE);
  }
  Node *temp = head;
  printf("List: ");
  do
  {
    printf("%d ", temp->value);
    temp = temp->next;
  } while (temp != head);
  printf("\n");
}

void clearList(Node **head)
{
  if (!head)
  {
    fprintf(stderr, "List is empty.\n");
    exit(EXIT_FAILURE);
  }

  (*head)->prev->next = NULL;
  Node *curr = *head;
  while (curr)
  {
    Node *next = curr->next;
    free(curr);
    curr = next;
  }
  *head = NULL;
}
