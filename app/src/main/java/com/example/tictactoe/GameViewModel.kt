package com.example.tictactoe

import androidx.compose.animation.core.StartOffsetType.Companion.Delay
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameViewModel : ViewModel() {
    var state by mutableStateOf(GameState())

    val boardItems: MutableMap<Int, BoardCellValue> = mutableMapOf(
        1 to BoardCellValue.NONE,
        2 to BoardCellValue.NONE,
        3 to BoardCellValue.NONE,
        4 to BoardCellValue.NONE,
        5 to BoardCellValue.NONE,
        6 to BoardCellValue.NONE,
        7 to BoardCellValue.NONE,
        8 to BoardCellValue.NONE,
        9 to BoardCellValue.NONE,
    )

    fun onAction(action: UserAction) {
        when (action) {
            is UserAction.BoardTapped -> {
                addValueToBoard(action.cellNo)
                if (!state.hasWon && !hasBoardFull()) {
                    viewModelScope.launch {Delay
                        delay(1000)
                        botMove()
                    }
                }
            }
            UserAction.PlayAgainButtonClicked -> {
                gameReset()
            }
        }
    }

    private fun gameReset() {
        boardItems.forEach { (i, _) ->
            boardItems[i] = BoardCellValue.NONE
        }
        // สลับคนเริ่ม
        val newTurn = if (state.lastStarter == BoardCellValue.CROSS || state.lastStarter == BoardCellValue.NONE) {
            BoardCellValue.CIRCLE
        } else {
            BoardCellValue.CROSS
        }

        val initialTurn = if (newTurn == BoardCellValue.CIRCLE) {
            "Player 'O'"
        } else {
            "BOT 'X'"
        }

        state = state.copy(
            hintText = "$initialTurn Turn!!",
            currentTurn = newTurn,
            lastStarter = newTurn,
            victoryType = VictoryType.NONE,
            hasWon = false
        )

        if (state.currentTurn == BoardCellValue.CROSS) {
            botMove()
        }
    }

    private fun addValueToBoard(cellNo: Int) {
        if (boardItems[cellNo] != BoardCellValue.NONE) {
            return
        }
        if (state.currentTurn == BoardCellValue.CIRCLE) {
            boardItems[cellNo] = BoardCellValue.CIRCLE
            state = if (checkForVictory(BoardCellValue.CIRCLE)) {
                state.copy(
                    hintText = "Player 'O' Won",
                    playerCircleCount = state.playerCircleCount + 1,
                    currentTurn = BoardCellValue.NONE,
                    hasWon = true
                )
            } else if (hasBoardFull()) {
                state.copy(
                    hintText = "Game Draw",
                    drawCount = state.drawCount + 1
                )
            } else {
                state.copy(
                    hintText = "BOT 'X' turn",
                    currentTurn = BoardCellValue.CROSS
                )
            }
        } else if (state.currentTurn == BoardCellValue.CROSS) {
            boardItems[cellNo] = BoardCellValue.CROSS
            state = if (checkForVictory(BoardCellValue.CROSS)) {
                state.copy(
                    hintText = "BOT 'X' Won",
                    playerCrossCount = state.playerCrossCount + 1,
                    currentTurn = BoardCellValue.NONE,
                    hasWon = true
                )
            } else if (hasBoardFull()) {
                state.copy(
                    hintText = "Game Draw",
                    drawCount = state.drawCount + 1
                )
            } else {
                state.copy(
                    hintText = "Player 'O' turn",
                    currentTurn = BoardCellValue.CIRCLE
                )
            }
        }
    }
    private fun botMove() {
        val emptyCells = boardItems.filter { it.value == BoardCellValue.NONE }.keys.toList()
        if (emptyCells.isNotEmpty()) {
            // (c) วางในช่องกลาง (หากว่ามีช่องกลาง)
            if (emptyCells.contains(5)) {
                addValueToBoard(5)
                return
            }
            // (a) ตรวจสอบว่าคอมพิวเตอร์สามารถชนะได้ในรอบนี้
            for (cell in emptyCells) {
                if (boardItems[cell] == BoardCellValue.NONE) {
                    // สร้างสถานการณ์ชั่วคราวเพื่อทดสอบการวางเครื่องหมาย CROSS
                    boardItems[cell] = BoardCellValue.CROSS
                    val canWin = checkForVictory(BoardCellValue.CROSS)
                    // คืนค่าสถานการณ์เดิมของช่อง
                    boardItems[cell] = BoardCellValue.NONE
                    if (canWin) {
                        addValueToBoard(cell)
                        return
                    }
                }
            }
            // (b) ตรวจสอบว่าผู้เล่น O สามารถชนะได้ในรอบถัดไปและบล็อก
            for (cell in emptyCells) {
                if (boardItems[cell] == BoardCellValue.NONE) {
                    // สร้างสถานการณ์ชั่วคราวเพื่อทดสอบการวางเครื่องหมาย CROSS
                    boardItems[cell] = BoardCellValue.CIRCLE
                    val canWin = checkForVictory(BoardCellValue.CIRCLE)
                    // คืนค่าสถานการณ์เดิมของช่อง
                    boardItems[cell] = BoardCellValue.NONE
                    if (canWin) {
                        addValueToBoard(cell)
                        return
                    }
                }
            }
            // (d) ถ้าไม่ได้ (สุ่มช่องที่จะวาง)
            val randomCell = emptyCells.random()
            addValueToBoard(randomCell)
        }
    }

    private fun checkForVictory(boardValue: BoardCellValue): Boolean {
        when {
            boardItems[1] == boardValue && boardItems[2] == boardValue && boardItems[3] == boardValue -> {
                state = state.copy(victoryType = VictoryType.HORIZONTAL1)
                return true
            }

            boardItems[4] == boardValue && boardItems[5] == boardValue && boardItems[6] == boardValue -> {
                state = state.copy(victoryType = VictoryType.HORIZONTAL2)
                return true
            }

            boardItems[7] == boardValue && boardItems[8] == boardValue && boardItems[9] == boardValue -> {
                state = state.copy(victoryType = VictoryType.HORIZONTAL3)
                return true
            }

            boardItems[1] == boardValue && boardItems[4] == boardValue && boardItems[7] == boardValue -> {
                state = state.copy(victoryType = VictoryType.VERTICAL1)
                return true
            }

            boardItems[2] == boardValue && boardItems[5] == boardValue && boardItems[8] == boardValue -> {
                state = state.copy(victoryType = VictoryType.VERTICAL2)
                return true
            }

            boardItems[3] == boardValue && boardItems[6] == boardValue && boardItems[9] == boardValue -> {
                state = state.copy(victoryType = VictoryType.VERTICAL3)
                return true
            }

            boardItems[1] == boardValue && boardItems[5] == boardValue && boardItems[9] == boardValue -> {
                state = state.copy(victoryType = VictoryType.DIAGONAL1)
                return true
            }

            boardItems[3] == boardValue && boardItems[5] == boardValue && boardItems[7] == boardValue -> {
                state = state.copy(victoryType = VictoryType.DIAGONAL2)
                return true
            }

            else -> return false
        }
    }
    internal fun hasBoardFull(): Boolean {
        return !boardItems.containsValue(BoardCellValue.NONE)
    }
}