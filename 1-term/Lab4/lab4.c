#include <stdio.h>
#include <windows.h>


void console_print() {
   printf("*");
   Sleep(10);
}


int main() {
    const int HEIGHT = 25;
    const int WIDTH = 80;
    HANDLE hConsole = GetStdHandle(STD_OUTPUT_HANDLE);
    COORD default_pos = { 0, HEIGHT - 1 };
    SetConsoleCursorPosition(hConsole, default_pos);
    console_print();
    for (int n = 1; n < 53; n++) {
        COORD start_upper_diagonal = {default_pos.X + 2*n - 1, default_pos.Y };
        SetConsoleCursorPosition(hConsole, start_upper_diagonal);
        for (int i = 0; i < 2*n; i++) {
            COORD upper_diagonal = {start_upper_diagonal.X - i, start_upper_diagonal.Y - i};
            if ( upper_diagonal.X > WIDTH - 1 || // If "*" goes out of window - continue
                 upper_diagonal.Y < 0 ||
                 upper_diagonal.X < 0 ||
                 upper_diagonal.Y > HEIGHT - 1) {
                continue;
                 }
            SetConsoleCursorPosition(hConsole, upper_diagonal);
            console_print();
        }
        COORD start_lower_diagonal = {default_pos.X, default_pos.Y - 2*n };
        SetConsoleCursorPosition(hConsole, start_lower_diagonal);
        for (int j = 0; j <= 2*n; j++) {
            COORD lower_diagonal = {start_lower_diagonal.X + j, start_lower_diagonal.Y + j};
            if ( lower_diagonal.X > WIDTH - 1 || // If "*" goes out of window - continue
                lower_diagonal.Y < 0 ||
                lower_diagonal.X < 0 ||
                lower_diagonal.Y > HEIGHT - 1) {
                continue;
                }
            SetConsoleCursorPosition(hConsole, lower_diagonal);
            console_print();
        }
    }
    Sleep(10000);
    return 0;
}
