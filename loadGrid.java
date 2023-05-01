package MineSweeper;

public class loadGrid {

    public static void main(String[] args) {
        String str = "??1               1?1             1?????\n" +
                "??1               1?1             111111\n" +
                "111 111           111                   \n" +
                "    1?1   111                           \n" +
                "1121211   1?1     111  111              \n" +
                "?????1    111     1?1  1?1              \n" +
                "1122?1            11211111              \n" +
                "   111              1?1                 \n" +
                "        1221        111         111     \n" +
                "111     2??1                    1?1     \n" +
                "??1     2?31                    111     \n" +
                "111     111                      111    \n" +
                "                                 1?211  \n" +
                "                                 112?1  \n" +
                "                                   111  \n" +
                "11  111    111                          \n" +
                "?1  1?1    1?1         111              \n" +
                "11  111    111      1111?1  111         \n" +
                "       111          1???21  1?1         \n" +
                "       1?1         12???1   111   111 11\n" +
                "       111         1?1111111      1?1 1?\n" +
                "11      111        111   1?1      111 11\n" +
                "?1      1?1              111            \n" +
                "11      111                             \n" +
                "                        111             \n" +
                "111      111      111   1?1             \n" +
                "??1      1?1  111 1?1   111          111\n" +
                "111      111  1?11221                2??\n" +
                "              1111?1                 2??\n" +
                "                 111                 111\n" +
                "                        111             \n" +
                "       11211            1?1             \n" +
                "      12???1            2?2             \n" +
                "      1?2211            1?1             \n" +
                "      111               111             \n" +
                "    111                            1221 \n" +
                "    1?1                            1??1 \n" +
                "    111                            1221 \n" +
                "         111                            \n" +
                "         1?1                            \n";


        int columnSize,rowSize;
        String [] temp = str.split("\n");
        rowSize = temp.length;
        columnSize = temp[0].length();
        int [][] grid = new int[rowSize][columnSize];
        for(int column = 0; column < columnSize; column++) {
            for (int row = 0; row < rowSize; row++) {
                //it is a space/empty tile
                if (temp[row].charAt(column) == ' ') {
                    grid[row][column] = -1;
                }

                //it is a numbered tile
                else if (Character.isDigit(temp[column].charAt(row))) {
                    grid[row][column] = temp[column].charAt(row) - '0';
                }

                //it is a closed tile
                else if (temp[column].charAt(row) == '?') {
                    grid[row][column] = -2;
                }

                //it is a flagged tile
                else if (temp[column].charAt(row) == 'X') {
                    grid[row][column] = -3;
                }
            }
        }


        for(int row = 0; row < grid.length;row++){
            for(int column = 0; column < grid[row].length; column++){
                System.out.print(grid[row][column]);
            }
            System.out.println();
        }

    }
}
