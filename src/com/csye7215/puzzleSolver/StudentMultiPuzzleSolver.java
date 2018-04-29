package com.csye7215.puzzleSolver;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This file needs to hold your solver to be tested. You can alter the class to
 * extend any class that extends PuzzleSolver. It must have a constructor that
 * takes in a Puzzle. It must have a solve() method that returns the datatype
 * List<Direction> which will either be a reference to a list of steps to take
 * or will be null if the puzzle cannot be solved.
 */
public class StudentMultiPuzzleSolver extends SkippingPuzzleSolver {
	public StudentMultiPuzzleSolver(Puzzle puzzle) {
		super(puzzle);
	}

	// Pre-Conditions: check if puzzle.length != 0
	// Post-Conditions: if solve() method returns null no solution found
	// Invariants: Puzzle, Direction
	public List<Direction> solve() {
		// TODO: Implement your code here

		int numProcessors = Runtime.getRuntime().availableProcessors();
		ExecutorService threadPool = Executors.newFixedThreadPool(numProcessors);

		List<Callable<List<Direction>>> tasks = new LinkedList<>();
		try {
			Choice start = firstChoice(puzzle.getStart());
			while (!start.choices.isEmpty()) {
				tasks.add(new Search(follow(start.at, start.choices.peek()), start.choices.pop()));
			}
		} catch (SolutionFound e) {
			System.out.println("Solution found");
		}
		List<Direction> solution = null;
		try {
			for(int i =0; i < tasks.size(); i++) {
				solution = threadPool.submit(tasks.get(i)).get();
				if(solution != null) {
					break;
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		threadPool.shutdown();
		return solution;
	}

	// implementing DFS
	class Search implements Callable<List<Direction>> {

		Choice start;
		Direction dir;

		public Search(Choice start, Direction dir) {
			this.start = start;
			this.dir = dir;
		}

		@Override
		public List<Direction> call() throws Exception {
			LinkedList<Choice> choiceStack = new LinkedList<Choice>();
			Choice ch;

			try {
				choiceStack.push(firstChoice(puzzle.getStart()));
				while (!choiceStack.isEmpty()) {
					ch = choiceStack.peek();
					if (ch.isDeadend()) {
						// backtrack.
						choiceStack.pop();
						if (!choiceStack.isEmpty())
							choiceStack.peek().choices.pop();
						continue;
					}
					choiceStack.push(follow(ch.at, ch.choices.peek()));
				}
				// No solution found.
				return null;
			} catch (SolutionFound e) {
				Iterator<Choice> iter = choiceStack.iterator();
				LinkedList<Direction> solutionPath = new LinkedList<Direction>();
				while (iter.hasNext()) {
					ch = iter.next();
					solutionPath.push(ch.choices.peek());
				}

				if (puzzle.display != null)
					puzzle.display.updateDisplay();
				return pathToFullPath(solutionPath);
			}

		}
	}
}