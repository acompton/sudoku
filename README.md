# Sudoku

This is a little Sudoku solver that I wrote for fun.

Given an unsolved Sudoku grid like this, it will find and print out the solution.  The grid can be provided in a text 
file or piped in through standard input. 

    _ _ 4   7 _ _   _ 6 1
    5 _ _   _ _ 2   _ 8 _
    _ _ _   _ _ 8   3 _ _
    
    _ 4 _   8 _ _   5 _ 6
    _ _ _   6 _ 9   _ _ _
    8 _ 6   _ _ 1   _ 9 _
    
    _ _ 8   5 _ _   _ _ _
    _ 5 _   1 _ _   _ _ 9
    6 1 _   _ _ 7   4 _ _
    
Only 9x9 grids are supported at this time.  When specifying a grid, numbers 1-9 indicate any known positions, and any other
value can be used to represent unsolved positions.  So the underscores above could be replaced with any other character. 
The parser ignores whitespace, so grids can also be specified inline.

    004700061500002080000008300040800506000609000806001090008500000050100009610007400
    
Running either of the above will print out this solution.  If the input has more than one solution (i.e., it is an 
invalid puzzle), then all solutions are displayed.

    ╔═══════╦═══════╦═══════╗
    ║ 9 8 4 ║ 7 3 5 ║ 2 6 1 ║
    ║ 5 6 3 ║ 4 1 2 ║ 9 8 7 ║
    ║ 7 2 1 ║ 9 6 8 ║ 3 4 5 ║
    ╠═══════╬═══════╬═══════╣
    ║ 2 4 9 ║ 8 7 3 ║ 5 1 6 ║
    ║ 1 7 5 ║ 6 4 9 ║ 8 3 2 ║
    ║ 8 3 6 ║ 2 5 1 ║ 7 9 4 ║
    ╠═══════╬═══════╬═══════╣
    ║ 4 9 8 ║ 5 2 6 ║ 1 7 3 ║
    ║ 3 5 7 ║ 1 8 4 ║ 6 2 9 ║
    ║ 6 1 2 ║ 3 9 7 ║ 4 5 8 ║
    ╚═══════╩═══════╩═══════╝
    