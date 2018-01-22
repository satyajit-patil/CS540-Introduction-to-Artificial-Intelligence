import java.util.*;
import java.io.*;

////////////////////////////////////////////////////////////////////////////
//                                                                       
// Code for HW1, Problem 2
//               Inducing Decision Trees
//               CS540 (Shavlik)
//Author:           Satyajit Patil
//Email:            spatil5@wisc.edu
//CS Login:         jit
//Lecturer's Name:  Jude Shavlik
////////////////////////////80 columns wide //////////////////////////////////

/**
 * This program builds and prints a decision tree using the ID3 algorithm. It
 * also classifies examples using the built decision tree.
 * 
 * PROGRAM PRINTS THE DECISION TREE IN A TEXT FILE NAMED 'RESULTS' WHICH SHOULD
 * BE STORED IN THE WORKSPACE
 * 
 * PROGRAM PRINTS THE INCORRECT EXAMPLES AND ACCURACY ON THE CONSOLE WINDOW
 * 
 * @author Satyajit Patil <spatil5@wisc.edu>
 * 
 */
public class BuildAndTestDecisionTree
{
	// Strings that contain the categories of the dataset
	private static String datasetCatOne;
	private static String datasetCatTwo;
	private static ArrayList<BinaryFeature> origFeatures;

	/**
	 * "Main" reads in the names of the files we want to use, then reads in
	 * their examples.
	 * 
	 * @param args
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException
	{
		if (args.length != 2)
		{
			System.err.println("You must call BuildAndTestDecisionTree as "
					+ "follows:\n\njava BuildAndTestDecisionTree "
					+ "<trainsetFilename> <testsetFilename>\n");
			System.exit(1);
		}

		// Read in the file names.
		String trainset = args[0];
		String testset = args[1];

		// Read in the examples from the files.
		ListOfExamples trainExamples = new ListOfExamples();
		ListOfExamples testExamples = new ListOfExamples();
		if (!trainExamples.ReadInExamplesFromFile(trainset)
				|| !testExamples.ReadInExamplesFromFile(testset))
		{
			System.err.println("Something went wrong reading the datasets ... "
					+ "giving up.");
			System.exit(1);
		} else
		{
			// turn features into an ArrayList
			ArrayList<BinaryFeature> features = new ArrayList<>();
			Collections.addAll(features, trainExamples.getFeatures());

			// ArrayList of original features so outputLabels of examples can be
			// indexed
			origFeatures = new ArrayList<>(features);

			// assign category values
			datasetCatOne = trainExamples.getOutputLabel().getFirstValue();
			// System.out.println(datasetCatOne);
			datasetCatTwo = trainExamples.getOutputLabel().getSecondValue();

			// create the root of the tree which is to be built
			InteriorNode root = new InteriorNode();
			root.list.addAll(trainExamples);

			// Build the decision tree
			root = BuildingDT(root, root.list, features, 0, root.list);

			// Print out the induced decision tree in a text file named
			// 'results.txt'
			File file = new File("results.txt");
			PrintWriter writer = new PrintWriter(file);
			PrintingDT(writer, root, "");
			writer.close();

			// Categorizes the TESTING SET using the induced tree
			ListOfExamples incorrect = new ListOfExamples();

			for (Example e : testExamples)
			{
				classify(root, e, origFeatures, incorrect);
			}

			// Prints out the names of examples incorrectly classified
			System.out.println("Names of Examples Incorrectly Classified: ");
			for (Example e : incorrect)
			{
				System.out.println(e.getName());
			}

			System.out.print("Accuracy: ");
			// Print out the fraction that were incorrectly classified
			double accuracy = 0;
			accuracy = (double) (testExamples.size() - incorrect.size())
					/ testExamples.size();
			System.out.println(accuracy);
		}

		Utilities.waitHere("Hit <enter> when ready to exit.");
	}

	/**
	 * Classifies an example using the induced decision tree
	 * 
	 * @param tree
	 *            induced tree
	 * @param test
	 *            example to be classified
	 * @param features
	 *            list of features to access the outputLabel of the example for
	 *            a particular feature
	 * @param incorrect
	 *            list of incorrectly classified examples
	 */
	public static void classify(InteriorNode tree, Example test,
			ArrayList<BinaryFeature> features, ListOfExamples incorrect)
	{
		// IF leaf node
		if (tree.getCategory() != null)
		{
			// IF incorrectly classified, add to 'incorrect'
			if (!test.getCategory().equals(tree.getCategory()))
			{
				incorrect.add(test);
			}
			return;
		}
		// ELSE go lower down the decision tree
		else
		{
			BinaryFeature bf = tree.getFeature();
			if (test.get(features.indexOf(bf)).equals(bf.getFirstValue()))
			{
				classify(tree.getLeft(), test, features, incorrect);
			} else if (test.get(features.indexOf(bf))
					.equals(bf.getSecondValue()))
			{
				classify(tree.getRight(), test, features, incorrect);
			}
		}
	}

	/**
	 * Print out the induced decision tree through pre-order traversal
	 * 
	 * @param w
	 *            printwriter
	 * @param node
	 *            current node being traversed
	 * @param indent
	 *            amount to be indented
	 */
	public static void PrintingDT(PrintWriter w, InteriorNode node,
			String indent)
	{
		if (node == null)
		{
			return;
		}
		// IF node is a leaf, print out the category of the node and the number
		// of examples
		else if (node.getCategory() != null)
		{
			w.println(indent + node.getCategory() + " (" + node.list.size()
					+ ")");
			return;
		} else
		{
			if (node.getLeft().getCategory() == null)
			{
				// Print the feature's name and the first value of the feature
				w.println(indent + node.getFeature().getName() + " = "
						+ node.getFeature().getFirstValue());
				// Recurse into the left subtree of the node
				PrintingDT(w, node.getLeft(), (indent + "|\t"));
			} else
			{
				// Print the feature's name and the first value of the feature
				w.print(indent + node.getFeature().getName() + " = "
						+ node.getFeature().getFirstValue());
				// Recurse into the left subtree of the node
				PrintingDT(w, node.getLeft(), ": ");
			}

			if (node.getRight().getCategory() == null)
			{
				// Print the feature's name and the second value of the feature
				w.println(indent + node.getFeature().getName() + " = "
						+ node.getFeature().getSecondValue());
				// Recurse into the right subtree of the node
				PrintingDT(w, node.getRight(), (indent + "|\t"));
			} else
			{
				// Print the feature's name and the second value of the feature
				w.print(indent + node.getFeature().getName() + " = "
						+ node.getFeature().getSecondValue());
				// Recurse into the right subtree of the node
				PrintingDT(w, node.getRight(), ": ");
			}
		}
	}

	/**
	 * Build the decision tree using
	 * 
	 * @param node
	 *            current node
	 * @param examples
	 *            examples within the tree/subtree
	 * @param features
	 *            list of BinaryFeature remaining to choose from
	 * @param targetFeature
	 *            index of the 'best' BinaryFeature
	 * @param parents
	 *            ListOfExamples in the node's parents
	 * @return a built tree
	 */
	public static InteriorNode BuildingDT(InteriorNode node,
			ListOfExamples examples, ArrayList<BinaryFeature> features,
			int targetFeature, ListOfExamples parents)
	{
		// IF there are no examples, set the node's category to the Majority
		// value
		// of it's parent
		if (examples.isEmpty())
		{
			node.setCategory(Majority(parents));
		}
		// ELSE IF all the examples have the same category, set the node's
		// category to that category
		else if (SameClass(examples))
		{
			node.setCategory(examples.get(0).getCategory());
		}
		// ELSE IF there are no features remaining,set the node's category to
		// the Majority value of it's examples
		else if (features.isEmpty())
		{
			node.setCategory(Majority(examples));
		}
		// ELSE
		else
		{
			// make a copy of the list of features
			ArrayList<BinaryFeature> copy = new ArrayList<BinaryFeature>(
					features);

			// Get and remove the best feature
			targetFeature = BestFeatureIndex(examples, copy);
			BinaryFeature bf = copy.get(targetFeature);
			copy.remove(targetFeature);

			node.setFeature(bf);
			node.setLeftLabel(bf.getFirstValue());
			node.setRightLabel(bf.getSecondValue());

			InteriorNode left = new InteriorNode();
			InteriorNode right = new InteriorNode();

			// index of bf in the original list of features
			int nodeIndex = origFeatures.indexOf(bf);

			// Add examples to their respective nodes
			for (Example e : examples)
			{
				if (e.get(nodeIndex).equals(bf.getFirstValue()))
				{
					left.list.add(e);
				} else
				{
					right.list.add(e);
				}
			}

			// Recurse over the left subtree
			node.setLeft(left);
			left = BuildingDT(left, left.list, copy, 0, examples);

			// Recurse over the right subtree
			node.setRight(right);
			right = BuildingDT(right, right.list, copy, 0, examples);

		}
		return node;

	}

	/**
	 * Calculate the entropy using the equation Entropy(S) = S -p(I) log2 p(I)
	 * 
	 * @param q
	 *            value to calculate entropy
	 * @return entropy value
	 */
	public static double Entropy(double q)
	{
		double entro = 0;

		if (q == 1 || q == 0)
		{
		} else
		{
			entro = -1 * ((q * (Math.log(q) / (Math.log(2)))
					+ (1 - q) * (Math.log(1 - q) / (Math.log(2)))));
		}
		return entro;

	}

	/**
	 * Gets the Remainder for a particular BinaryFeature
	 * 
	 * @param bf
	 *            the BinaryFeature
	 * @param bfIndex
	 *            Index of the BinaryFeature
	 * @param examples
	 *            ListOfExample for which the remainder is being calculated
	 * @return the remainder
	 */
	public static double Remainder(BinaryFeature bf, ListOfExamples examples)
	{
		String labelOne = bf.getFirstValue();

		double labelOneOcc = 0;
		double labelOnePos = 0;

		double labelTwoOcc = 0;
		double labelTwoPos = 0;

		double remainder = 0;

		int exampleIndex = origFeatures.indexOf(bf);

		// Calculate the occurrences of labelOne, labelOne with one of the
		// category values, labelTwo, and labelTwo with one of the category
		// values
		for (Example e : examples)
		{
			if (e.get(exampleIndex).equals(labelOne))
			{
				labelOneOcc++;
				if (e.getCategory().equals(datasetCatOne))
				{
					labelOnePos++;
				}
			} else
			{
				labelTwoOcc++;
				if (e.getCategory().equals(datasetCatOne))
				{
					labelTwoPos++;
				}
			}
		}

		// Calculate and return the remainder
		if (labelOneOcc == 0 && !examples.isEmpty())
		{
			remainder = Entropy((labelTwoPos / labelTwoOcc));
		} else if (labelTwoOcc == 0 && !examples.isEmpty())
		{
			remainder = Entropy((labelOnePos / labelOneOcc));
		} else
		{
			remainder = ((labelOneOcc / examples.size())
					* Entropy((labelOnePos / labelOneOcc)))
					+ ((labelTwoOcc / examples.size())
							* Entropy((labelTwoPos / labelTwoOcc)));
		}

		return remainder;
	}

	/**
	 * Gets the index of the best feature
	 * 
	 * @param examples
	 *            ListOfExamples from which to choose best feature
	 * @param features
	 *            the different features
	 * @return index of the best feature
	 */
	public static int BestFeatureIndex(ListOfExamples examples,
			ArrayList<BinaryFeature> features)
	{
		int index = 0;
		int bfIndex = 0;
		double remainder = 1;

		for (BinaryFeature bf : features)
		{
			bfIndex = features.indexOf(bf);
			if (Remainder(bf, examples) < remainder)
			{
				remainder = Remainder(bf, examples);
				index = bfIndex;
			}
		}
		return index;
	}

	/**
	 * Gets the Majority category from a ListOfExamples
	 * 
	 * @param examples
	 *            ListOfExamples
	 * @return the category
	 */
	public static String Majority(ListOfExamples examples)
	{
		int categoryOne = 0;
		int categoryTwo = 0;

		for (Example e : examples)
		{
			if (e.getCategory().equals(datasetCatOne))
			{
				categoryOne++;
			} else
			{
				categoryTwo++;
			}
		}

		if (categoryOne > categoryTwo)
		{
			return datasetCatOne;
		} else
		{
			return datasetCatTwo;
		}

	}

	/**
	 * Determines whether all the examples in a ListOfExamples have the same
	 * category
	 * 
	 * @param examples
	 *            ListOfExamples
	 * @return true or false
	 */
	public static boolean SameClass(ListOfExamples examples)
	{
		boolean sameClass = true;
		String category = examples.get(0).getCategory();

		for (Example e : examples)
		{
			if (!e.getCategory().equals(category))
			{
				sameClass = false;
				break;
			}
		}

		return sameClass;
	}
}

/**
 * Node from which the Tree is built
 */
class InteriorNode
{
	// Category is not null of the node is leaf node
	String category;

	InteriorNode left;
	String leftLabel;

	InteriorNode right;
	String rightLabel;

	// feature of this node if it not a leaf node
	BinaryFeature feature;

	// examples stored in this node
	ListOfExamples list = new ListOfExamples();

	public InteriorNode()
	{
		left = null;
		right = null;
		category = null;
		leftLabel = null;
		rightLabel = null;
	}

	void setCategory(String s)
	{
		category = s;
	}

	String getCategory()
	{
		return category;
	}

	void setLeft(InteriorNode l)
	{
		left = l;
	}

	InteriorNode getLeft()
	{
		return left;
	}

	void setRight(InteriorNode l)
	{
		right = l;
	}

	InteriorNode getRight()
	{
		return right;
	}

	void setLeftLabel(String s)
	{
		leftLabel = s;
	}

	String getLeftLabel()
	{
		return leftLabel;
	}

	void setRightLabel(String s)
	{
		rightLabel = s;
	}

	String getRightLabel()
	{
		return rightLabel;
	}

	void setFeature(BinaryFeature b)
	{
		feature = b;
	}

	BinaryFeature getFeature()
	{
		return feature;
	}

	void addExample(Example e)
	{
		list.add(e);
	}

}

// This class, an extension of ArrayList, holds an individual example.
// The new method PrintFeatures() can be used to
// display the contents of the example.
// The items in the ArrayList are the feature values.
class Example extends ArrayList<String>
{
	// The name of this example.
	private String name;

	// The output label of this example.
	private String category;

	// The data set in which this is one example.
	private ListOfExamples parent;

	// Constructor which stores the dataset which the example belongs to.
	public Example(ListOfExamples parent)
	{
		this.parent = parent;
	}

	// Print out this example in human-readable form.
	public void PrintFeatures()
	{
		System.out.print("Example " + name + ",  label = " + category + "\n");
		for (int i = 0; i < parent.getNumberOfFeatures(); i++)
		{
			System.out.print("     " + parent.getFeatureName(i) + " = "
					+ this.get(i) + "\n");
		}
	}

	// Adds a feature value to the example.
	public void addFeatureValue(String value)
	{
		this.add(value);
	}

	// Accessor methods.
	public String getName()
	{
		return name;
	}

	public String getCategory()
	{
		return category;
	}

	// Mutator methods.
	public void setName(String name)
	{
		this.name = name;
	}

	public void setCategory(String label)
	{
		this.category = label;
	}
}

/*
 * This class holds all of our examples from one dataset (train OR test, not
 * BOTH). It extends the ArrayList class. Be sure you're not confused. We're
 * using TWO types of ArrayLists. An Example is an ArrayList of feature values,
 * while a ListOfExamples is an ArrayList of examples. Also, there is one
 * ListOfExamples for the TRAINING SET and one for the TESTING SET.
 */
class ListOfExamples extends ArrayList<Example>
{
	// The name of the dataset.
	private String nameOfDataset = "";

	// The number of features per example in the dataset.
	private int numFeatures = -1;

	// An array of the parsed features in the data.
	private BinaryFeature[] features;

	// A binary feature representing the output label of the dataset.
	private BinaryFeature outputLabel;

	// The number of examples in the dataset.
	private int numExamples = -1;

	public ListOfExamples()
	{
	}

	public BinaryFeature getOutputLabel()
	{
		return outputLabel;
	}

	// Print out a high-level description of the dataset including its features.
	public void DescribeDataset()
	{
		System.out.println(
				"Dataset '" + nameOfDataset + "' contains " + numExamples
						+ " examples, each with " + numFeatures + " features.");
		System.out
				.println("Valid category labels: " + outputLabel.getFirstValue()
						+ ", " + outputLabel.getSecondValue());
		System.out
				.println("The feature names (with their possible values) are:");
		for (int i = 0; i < numFeatures; i++)
		{
			BinaryFeature f = features[i];
			System.out.println("   " + f.getName() + " (" + f.getFirstValue()
					+ " or " + f.getSecondValue() + ")");
		}
		System.out.println();
	}

	// Print out ALL the examples.
	public void PrintAllExamples()
	{
		System.out.println("List of Examples\n================");
		for (int i = 0; i < size(); i++)
		{
			Example thisExample = this.get(i);
			thisExample.PrintFeatures();
		}
	}

	// Print out the SPECIFIED example.
	public void PrintThisExample(int i)
	{
		Example thisExample = this.get(i);
		thisExample.PrintFeatures();
	}

	// Returns the number of features in the data.
	public int getNumberOfFeatures()
	{
		return numFeatures;
	}

	public BinaryFeature[] getFeatures()
	{
		return features;
	}

	// Returns the name of the ith feature.
	public String getFeatureName(int i)
	{
		return features[i].getName();
	}

	// Takes the name of an input file and attempts to open it for parsing.
	// If it is successful, it reads the dataset into its internal structures.
	// Returns true if the read was successful.
	public boolean ReadInExamplesFromFile(String dataFile)
	{
		nameOfDataset = dataFile;

		// Try creating a scanner to read the input file.
		Scanner fileScanner = null;
		try
		{
			fileScanner = new Scanner(new File(dataFile));
		} catch (FileNotFoundException e)
		{
			return false;
		}

		// If the file was successfully opened, read the file
		this.parse(fileScanner);
		return true;
	}

	/**
	 * Does the actual parsing work. We assume that the file is in proper
	 * format.
	 *
	 * @param fileScanner
	 *            a Scanner which has been successfully opened to read the
	 *            dataset file
	 */
	public void parse(Scanner fileScanner)
	{
		// Read the number of features per example.
		numFeatures = Integer.parseInt(parseSingleToken(fileScanner));

		// Parse the features from the file.
		parseFeatures(fileScanner);

		// Read the two possible output label values.
		String labelName = "output";
		String firstValue = parseSingleToken(fileScanner);
		String secondValue = parseSingleToken(fileScanner);
		outputLabel = new BinaryFeature(labelName, firstValue, secondValue);

		// Read the number of examples from the file.
		numExamples = Integer.parseInt(parseSingleToken(fileScanner));

		parseExamples(fileScanner);
	}

	/**
	 * Returns the first token encountered on a significant line in the file.
	 *
	 * @param fileScanner
	 *            a Scanner used to read the file.
	 */
	private String parseSingleToken(Scanner fileScanner)
	{
		String line = findSignificantLine(fileScanner);

		// Once we find a significant line, parse the first token on the
		// line and return it.
		Scanner lineScanner = new Scanner(line);
		return lineScanner.next();
	}

	/**
	 * Reads in the feature metadata from the file.
	 * 
	 * @param fileScanner
	 *            a Scanner used to read the file.
	 */
	private void parseFeatures(Scanner fileScanner)
	{
		// Initialize the array of features to fill.
		features = new BinaryFeature[numFeatures];

		for (int i = 0; i < numFeatures; i++)
		{
			String line = findSignificantLine(fileScanner);

			// Once we find a significant line, read the feature description
			// from it.
			Scanner lineScanner = new Scanner(line);
			String name = lineScanner.next();
			String dash = lineScanner.next(); // Skip the dash in the file.
			String firstValue = lineScanner.next();
			String secondValue = lineScanner.next();
			features[i] = new BinaryFeature(name, firstValue, secondValue);
		}
	}

	private void parseExamples(Scanner fileScanner)
	{
		// Parse the expected number of examples.
		for (int i = 0; i < numExamples; i++)
		{
			String line = findSignificantLine(fileScanner);
			Scanner lineScanner = new Scanner(line);

			// Parse a new example from the file.
			Example ex = new Example(this);

			String name = lineScanner.next();
			ex.setName(name);

			String label = lineScanner.next();
			ex.setCategory(label);

			// Iterate through the features and increment the count for any
			// feature
			// that has the first possible value.
			for (int j = 0; j < numFeatures; j++)
			{
				String feature = lineScanner.next();
				ex.addFeatureValue(feature);
			}

			// Add this example to the list.
			this.add(ex);
		}
	}

	/**
	 * Returns the next line in the file which is significant (i.e. is not all
	 * whitespace or a comment.
	 *
	 * @param fileScanner
	 *            a Scanner used to read the file
	 */
	private String findSignificantLine(Scanner fileScanner)
	{
		// Keep scanning lines until we find a significant one.
		while (fileScanner.hasNextLine())
		{
			String line = fileScanner.nextLine().trim();
			if (isLineSignificant(line))
			{
				return line;
			}
		}

		// If the file is in proper format, this should never happen.
		System.err.println("Unexpected problem in findSignificantLine.");

		return null;
	}

	/**
	 * Returns whether the given line is significant (i.e., not blank or a
	 * comment). The line should be trimmed before calling this.
	 *
	 * @param line
	 *            the line to check
	 */
	private boolean isLineSignificant(String line)
	{
		// Blank lines are not significant.
		if (line.length() == 0)
		{
			return false;
		}

		// Lines which have consecutive forward slashes as their first two
		// characters are comments and are not significant.
		if (line.length() > 2 && line.substring(0, 2).equals("//"))
		{
			return false;
		}

		return true;
	}
}

/**
 * Represents a single binary feature with two String values.
 */
class BinaryFeature
{
	private String name;
	private String firstValue;
	private String secondValue;

	public BinaryFeature(String name, String first, String second)
	{
		this.name = name;
		firstValue = first;
		secondValue = second;
	}

	public String getName()
	{
		return name;
	}

	public String getFirstValue()
	{
		return firstValue;
	}

	public String getSecondValue()
	{
		return secondValue;
	}
}

class Utilities
{
	// This method can be used to wait until you're ready to proceed.
	public static void waitHere(String msg)
	{
		System.out.print("\n" + msg);
		try
		{
			System.in.read();
		} catch (Exception e)
		{
		} // Ignore any errors while reading.
	}
}