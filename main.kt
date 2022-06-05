class Board {
    var board: MutableList<Char> = ('1'..'9').toMutableList()
    fun show() {
        for (i in 1..board.size) {
            print("  ${board[i-1]}")
            if (i % 3 == 0) println()
        }
    }

    fun fieldUpdate(symbol: Char, number: Char)
    {
        board[number.toString().toInt()-1] = symbol
    }

    fun evaluate(symbol: Char): Int
    {
        fun getMoveValue(index: Int): Int
        {
            if(board[index] == symbol)
                return 1
            return -1
        }

        for(i in 0 until 3) //horizontally
        { 
            if(setOf(board[i*3], board[i*3+1], board[i*3+2]).size == 1)
                return getMoveValue(i*3)
        }

        for(i in 0 until 3) //vertically
        {
            if(setOf(board[i], board[i+3], board[i+6]).size == 1)
                return getMoveValue(i)
        }
        //across
        if(setOf(board[0], board[4], board[8]).size == 1)
        {
            return getMoveValue(0)
        }
        if(setOf(board[2], board[4], board[6]).size == 1)
        {
            return getMoveValue(2)
        }
        if(!board.any{it.isDigit()}) //draw
            return 2
        return 0
    }

    fun winCheck(symbol: Char): Boolean
    {
        val x = evaluate(symbol)
        if(Math.abs(x)==1 || x == 2)
        { 
            println(mapOf(-1 to "Wygrał ${listOf('X','O')[(listOf('X','O').indexOf(symbol)+1)%2]}", 2 to "Remis", 1 to "Wygrał $symbol")[x]) 
            return true 
        }
        return false
        
    }
}

class Game {
    private var board: Board = Board()
    private var players: MutableList<Player> = mutableListOf()
    private var actual_player: Int = 0

    fun startGame()
    {
        board.show()
        
        for(i in 1..9)
        {
            println("Podaj numer pola dla ${players[actual_player].symbol}: ")
            board.fieldUpdate(players[actual_player].symbol, players[actual_player].move(board.board))
            board.show()
            if(board.winCheck(players[actual_player].symbol))
                break;
            actual_player = (actual_player+1)%2
        }
    }
    
    fun selectType(j: Int)
    {
        for(i in 0..1)
        {
            println("Wybierz typ "+listOf("pierwszego", "drugiego")[i]+" gracza(dostępne: H - człowiek, R - random, A - AI: ")
            do{
                var x: Char = ' '
                try {
                    val y = readLine()
                    if(y?.length == 1)
                        x = y[0].toUpperCase()
                } catch (e: Exception) {
                    println("Musisz wpisać odpowiedni typ gracza")
                    continue
                } 
                    if (listOf('H', 'R', 'A').contains(x)) {
                        println("$x został wybrany")
                        when(x)
                        {
                            'H'->players.add(HumanPlayer(listOf('X','O')[(i+j)%2]))
                            'R'->players.add(RandomPlayer(listOf('X','O')[(i+j)%2]))
                            'A'->players.add(AIPlayer(listOf('X','O')[(i+j)%2]))
                        }
                        break
                    } else println("Musisz wpisać H, R lub A")
                
            }while(true)
        }
    }

    init 
    {
        println("Witaj w kółko i krzyżyk! Wybierz symbol pierwszego gracza: ")
        var symbol: Char = ' '
        do {
            try {
                val y = readLine()
                if(y?.length == 1)
                    symbol = y[0].toUpperCase()
            } catch (e: Exception) {
                println("Musisz wpisać X lub O")
                continue
            } 
                if (symbol == 'X' || symbol == 'O') {
                    println("${symbol} został wybrany")
                    break
                } else println("Musisz wpisać X lub O")
            
        } while (true)
        selectType(listOf('X','O').indexOf(symbol))
        startGame()
    }
}

abstract class Player(symbol: Char) {
    var symbol: Char
    init {
        this.symbol = symbol
    }
    
    abstract fun move(board: List<Char>): Char
}

class HumanPlayer(symbol: Char) : Player(symbol) {
    override fun move(board: List<Char>): Char
    {
        do
        {
            val input = readLine()
            var number: Char
            try{
                input!![0].toString().toInt()
                number = input[0]
            }
            catch(e: Exception){
                println("Niepoprawne dane")
                continue
            }
            if(input.length != 1)
            {
                println("Niepoprawne dane")
                continue
            }
            if(number.toString().toInt() < 1 || number.toString().toInt() > 9)
                println("Numer pola musi być w zakresie od 1 do 9")
            else if(!board.contains(number))
                println("To pole jest już zajęte")
            else
                return number
        }while(true)
    }
}

class RandomPlayer(symbol: Char) : Player(symbol) {
    override fun move(board: List<Char>): Char
    {
        return (board.filter{it.isDigit()}).random()
    }
}

class AIPlayer(symbol: Char) : Player(symbol) {
    private var board: Board = Board()

    private fun minimax(depth: Int, maximizingPlayer: Boolean): Int
    {
        return alphabeta(depth, maximizingPlayer, -10, 10)
    }

    private fun alphabeta(depth: Int, maximizingPlayer: Boolean, alpha: Int, beta: Int): Int
    {
        val score = board.evaluate(this.symbol)
        if(Math.abs(score) == 1)
            return score
        else if(depth == 0)
            return 0
        if(maximizingPlayer)
        {
            var alpha_ = -10
   
            for(i in board.board.filter{it.isDigit()})
            {
                board.fieldUpdate(symbol, i)
                alpha_ = Math.max(alpha, Math.max(alpha_, alphabeta(depth - 1, false, alpha, beta)))
                board.fieldUpdate(i, i)
                if(beta <= alpha_)
                    break
            }
            return alpha_

        }
        else
        {
            var beta_ = 10

            for(i in board.board.filter{it.isDigit()})
            {
                board.fieldUpdate(listOf('X','O')[(listOf('X','O').indexOf(symbol)+1)%2], i)
                beta_ = Math.min(beta, Math.min(beta_, alphabeta(depth - 1, true, alpha, beta)))
                board.fieldUpdate(i, i)
                if(beta_ <= alpha)
                    break
            }
            return beta_
        }
    }

    override fun move(board: List<Char>): Char
    {
        this.board.board = board.toMutableList()
        var best: Pair<Int, Char> = Pair(-10, ' ')
        for(i in this.board.board.filter{it.isDigit()})
        {
            this.board.fieldUpdate(symbol, i)
            val score = minimax(this.board.board.filter{it.isDigit()}.size,false)
            this.board.fieldUpdate(i, i)

            if(best.first < score)
                best = Pair(score, i)
        }
        return best.second
    }
}

fun main() {
    Game()
}
