/**
 * Cengiz Bilal Sarı
 * 26.03.2023
 * This project is designed to create Istanbul subway map and find the way which the user should follow in subways.
 * At the end draw the graph with std graph library of java and print the way.
 */

import java.awt.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.io.File;

public class IstanbulSubwayMap {
    public static void main(String[] args) throws FileNotFoundException {
        //File name of the input file
        String fileName = "coordinates.txt";
        //File object is required to open the file
        File file = new File(fileName);
        //Scanner object is required to read the content of the file
        Scanner inputFile = new Scanner(file);
        //Arrays for breakPoints
        ArrayList<ArrayList<String>> breakPoints = new ArrayList<>();

        //Arrays for metroLines
        ArrayList<String> justLineNames = new ArrayList<>();
        ArrayList<ArrayList<String>> colorsOfLines = new ArrayList<>();

        //Arrays for allStationLines
        ArrayList<ArrayList<String>> justNamesOfStations = new ArrayList<>();
        ArrayList<ArrayList<String>> justCoordinatesOfStations = new ArrayList<>();

        int counterForRows = 0;
        //continue reading file contents if there is a line to be read , it is data taking part
        while (inputFile.hasNextLine()) {
            String newLine = inputFile.nextLine();
            if (counterForRows > 19) {
                ArrayList<String> breakPoint = new ArrayList<>();
                String[] strBreakPoint = newLine.split(" ");
                for (String e : strBreakPoint) {
                    breakPoint.add(e);
                }
                breakPoints.add(breakPoint);
            } else {
                ArrayList<String> justCoordinateOfStation = new ArrayList<>();
                if (counterForRows % 2 == 0) {
                    String[] strParts = newLine.split(" ");
                    String[] colors = strParts[1].split(",");
                    justLineNames.add(strParts[0]);
                    ArrayList<String> colorsOfLine = new ArrayList<>();
                    for (String e : colors) {
                        colorsOfLine.add(e);
                    }
                    colorsOfLines.add(colorsOfLine);
                } else {
                    ArrayList<String> justNameOfStations = new ArrayList<>();
                    String[] strParts2 = newLine.split(" ");

                    for (int j = 0; j < strParts2.length; j++) {

                        if (j % 2 == 0) {
                            justNameOfStations.add(strParts2[j]);
                        } else {
                            String[] strParts3 = strParts2[j].split(",");

                            for (String e : strParts3) {
                                justCoordinateOfStation.add(e);
                            }
                        }
                    }
                    justCoordinatesOfStations.add(justCoordinateOfStation);
                    justNamesOfStations.add(justNameOfStations);
                }
                counterForRows++;
            }
        }
        inputFile.close();
        //The end of the data taking part

        boolean willWeDraw = false;
        ArrayList<ArrayList<String>> justStationNamesWithoutStar = new ArrayList<>();

        // with clone method and for loop the code copies the array list without star
        for (int i = 0; i < justNamesOfStations.size(); i++) {
            ArrayList<String> a = new ArrayList<>(justNamesOfStations.get(i));
            justStationNamesWithoutStar.add((ArrayList) a.clone());
        }
        for (int i = 0; i < justNamesOfStations.size(); i++) {
            for (int j = 0; j < justNamesOfStations.get(i).size(); j++) {
                if (justNamesOfStations.get(i).get(j).charAt(0) == '*') {
                    String stringWithoutStar = justNamesOfStations.get(i).get(j).substring(1);
                    justStationNamesWithoutStar.get(i).set(j, stringWithoutStar);
                }
            }
        }

        //Take the stations from the user
        Scanner input = new Scanner(System.in);
        String firstStation = input.next();
        String secondStation = input.next();

        if (!(checkStation(firstStation, justNamesOfStations) && checkStation(secondStation, justNamesOfStations))) {
            System.out.println("The station names provided are not present in this map.");
        } else {
            // creating neighbourLines lists
            ArrayList<ArrayList<String>> neighbours = neighbourLines(justLineNames, breakPoints);

            int indexOfWhichLineIsFirstStation = 0;
            int indexOfWhichLineIsSecondStation = 0;
            int indexOfWhereFirstStationIsExactly = 0;
            int indexOfWhereSecondStationIsExactly = 0;
            // with this for loop, we find the exact location of both of the point
            for (int i = 0; i < justStationNamesWithoutStar.size(); i++) {
                for (int j = 0; j < justStationNamesWithoutStar.get(i).size(); j++) {
                    if (firstStation.equals(justStationNamesWithoutStar.get(i).get(j))) {
                        indexOfWhichLineIsFirstStation = i;
                        indexOfWhereFirstStationIsExactly = j;
                    } else if (secondStation.equals(justStationNamesWithoutStar.get(i).get(j))) {
                        indexOfWhichLineIsSecondStation = i;
                        indexOfWhereSecondStationIsExactly = j;
                    }
                }
            }
            ArrayList<String> namesOfTheStationWeHaveToGo = new ArrayList<String>();
            //if they are in the same line , we find the way
            if (indexOfWhichLineIsFirstStation == indexOfWhichLineIsSecondStation) {
                willWeDraw = true;

                if (indexOfWhereSecondStationIsExactly > indexOfWhereFirstStationIsExactly) {
                    for (int i = indexOfWhereFirstStationIsExactly; i <= indexOfWhereSecondStationIsExactly; i++) {
                        namesOfTheStationWeHaveToGo.add(justStationNamesWithoutStar.get(indexOfWhichLineIsFirstStation).get(i));
                    }
                } else {
                    for (int i = indexOfWhereFirstStationIsExactly; i >= indexOfWhereSecondStationIsExactly; i--) {
                        namesOfTheStationWeHaveToGo.add(justStationNamesWithoutStar.get(indexOfWhichLineIsFirstStation).get(i));
                    }
                }
            } else {
                // if they are not in the same line , we should also find the way
                boolean[] visited = new boolean[justLineNames.size()];
                ArrayList<String> path = new ArrayList<>();
                path.add(justLineNames.get(indexOfWhichLineIsFirstStation));
                ArrayList<String> metroLinesSystemToFindPath = new ArrayList<>();
                findingPathAlgorithm(indexOfWhichLineIsFirstStation, visited, path, indexOfWhichLineIsSecondStation, neighbours, justLineNames, 0, metroLinesSystemToFindPath);
                if (!metroLinesSystemToFindPath.isEmpty()) {
                    willWeDraw = true;

                    ArrayList<String> breakPointWhichWeWillUse = new ArrayList<>(metroLinesSystemToFindPath.size() - 1);
                    ArrayList<Integer> indexOfLinesWhichWeWillUse = new ArrayList<>(metroLinesSystemToFindPath.size());
                    ArrayList<Integer> indexOfBreakPointsWhichWeWillUse = new ArrayList<>((metroLinesSystemToFindPath.size() - 1));

                    // to find breakPointsNames which we will use
                    for (int i = 0; i < metroLinesSystemToFindPath.size() - 1; i++) {
                        for (int j = 0; j < breakPoints.size(); j++) {
                            if (breakPoints.get(j).contains(metroLinesSystemToFindPath.get(i)) && breakPoints.get(j).contains(metroLinesSystemToFindPath.get(i + 1))) {
                                String breakpoint = breakPoints.get(j).get(0);
                                breakPointWhichWeWillUse.add(breakpoint);
                            }
                        }
                    }


                    // to find index of lines which we will use
                    for (int i = 0; i < metroLinesSystemToFindPath.size(); i++) {
                        for (int j = 0; j < justLineNames.size(); j++) {
                            if (metroLinesSystemToFindPath.get(i).equals(justLineNames.get(j))) {
                                indexOfLinesWhichWeWillUse.add(j);
                            }
                        }
                    }

                    // to find indexes of breakPoints which we will use
                    for (int i = 0; i < indexOfLinesWhichWeWillUse.size(); i++) {
                        int lineWeAreUsing = indexOfLinesWhichWeWillUse.get(i);
                        if (i == 0) {
                            for (int j = 0; j < justStationNamesWithoutStar.get(lineWeAreUsing).size(); j++) {
                                if (justStationNamesWithoutStar.get(lineWeAreUsing).get(j).equals(breakPointWhichWeWillUse.get(i))) {
                                    indexOfBreakPointsWhichWeWillUse.add(j);

                                }
                            }
                        } else if (i == indexOfLinesWhichWeWillUse.size() - 1) {
                            for (int j = 0; j < justStationNamesWithoutStar.get(lineWeAreUsing).size(); j++) {
                                if (justStationNamesWithoutStar.get(lineWeAreUsing).get(j).equals(breakPointWhichWeWillUse.get(i - 1))) {
                                    indexOfBreakPointsWhichWeWillUse.add(j);
                                }
                            }
                        } else {
                            for (int j = 0; j < justStationNamesWithoutStar.get(lineWeAreUsing).size(); j++) {
                                if (justStationNamesWithoutStar.get(lineWeAreUsing).get(j).equals(breakPointWhichWeWillUse.get(i - 1))) {
                                    indexOfBreakPointsWhichWeWillUse.add(j);

                                }
                            }


                            for (int j = 0; j < justStationNamesWithoutStar.get(lineWeAreUsing).size(); j++) {
                                if (justStationNamesWithoutStar.get(lineWeAreUsing).get(j).equals(breakPointWhichWeWillUse.get(i))) {
                                    indexOfBreakPointsWhichWeWillUse.add(j);

                                }
                            }
                        }
                    }


                    //printing and keeping stations in list which the  way has.
                    for (int i = 0; i < indexOfLinesWhichWeWillUse.size(); i++) {
                        if (i == 0) {
                            if (indexOfWhereFirstStationIsExactly < indexOfBreakPointsWhichWeWillUse.get(i)) {
                                for (int k = indexOfWhereFirstStationIsExactly; k <= indexOfBreakPointsWhichWeWillUse.get(i); k++) {
                                    namesOfTheStationWeHaveToGo.add(justStationNamesWithoutStar.get(indexOfLinesWhichWeWillUse.get(i)).get(k));
                                }
                            } else {
                                for (int k = indexOfWhereFirstStationIsExactly; k >= indexOfBreakPointsWhichWeWillUse.get(i); k--) {
                                    namesOfTheStationWeHaveToGo.add(justStationNamesWithoutStar.get(indexOfLinesWhichWeWillUse.get(i)).get(k));
                                }
                            }
                        } else if (i == indexOfLinesWhichWeWillUse.size() - 1) {
                            if (indexOfWhereSecondStationIsExactly < indexOfBreakPointsWhichWeWillUse.get(2 * i - 1)) {
                                for (int k = indexOfBreakPointsWhichWeWillUse.get(2 * i - 1) - 1; k >= indexOfWhereSecondStationIsExactly; k--) {
                                    namesOfTheStationWeHaveToGo.add(justStationNamesWithoutStar.get(indexOfLinesWhichWeWillUse.get(i)).get(k));
                                }
                            } else {
                                for (int k = indexOfBreakPointsWhichWeWillUse.get(2 * i - 1) + 1; k <= indexOfWhereSecondStationIsExactly; k++) {
                                    namesOfTheStationWeHaveToGo.add(justStationNamesWithoutStar.get(indexOfLinesWhichWeWillUse.get(i)).get(k));
                                }
                            }

                        } else {
                            if (indexOfBreakPointsWhichWeWillUse.get(2 * i - 1) < indexOfBreakPointsWhichWeWillUse.get(2 * i)) {
                                for (int k = indexOfBreakPointsWhichWeWillUse.get(2 * i - 1) + 1; k < indexOfBreakPointsWhichWeWillUse.get(2 * i) + 1; k++) {
                                    namesOfTheStationWeHaveToGo.add(justStationNamesWithoutStar.get(indexOfLinesWhichWeWillUse.get(i)).get(k));
                                }
                            } else {
                                for (int k = indexOfBreakPointsWhichWeWillUse.get(2 * i - 1) - 1; k >= indexOfBreakPointsWhichWeWillUse.get(2 * i); k--) {
                                    namesOfTheStationWeHaveToGo.add(justStationNamesWithoutStar.get(indexOfLinesWhichWeWillUse.get(i)).get(k));
                                }
                            }
                        }
                    }
                } else {
                    System.out.println("These two stations are not connected");
                }
            }
            if (willWeDraw) {

                for (String station : namesOfTheStationWeHaveToGo) {
                    System.out.println(station);
                }
                // If the way is found , the code has to pop up and do the animation staff.

                    /*Firstly it sets the canvas then creates the array list to coordinate of stations which it will use.
                    It pops up the main graphs and enters the for loop, with for loop animation part is initialized.
                    The process is like that: Draw the circle, clear all, draw all graph again and the circle with halved radius.
                    */

                StdDraw.setCanvasSize(1024, 482);
                StdDraw.setXscale(0, 1024);
                StdDraw.setYscale(0, 482);
                ArrayList<Integer> coordinateOfStationsWeWillUse = new ArrayList<>();
                for (int i = 0; i < namesOfTheStationWeHaveToGo.size(); i++) {
                    String station = namesOfTheStationWeHaveToGo.get(i);
                    for (int j = 0; j < justStationNamesWithoutStar.size(); j++) {
                        int indexOfStation = justStationNamesWithoutStar.get(j).indexOf(station);
                        if (indexOfStation != -1) {
                            coordinateOfStationsWeWillUse.add(Integer.parseInt(justCoordinatesOfStations.get(j).get(2 * indexOfStation)));
                            coordinateOfStationsWeWillUse.add(Integer.parseInt(justCoordinatesOfStations.get(j).get(2 * indexOfStation + 1)));
                            break;
                        }
                    }
                }
                int pauseDuration = 300;
                ArrayList<Double> stationsWhichIsPassed = new ArrayList<Double>(justCoordinatesOfStations.size() / 2);
                StdDraw.picture(512, 241, "background.jpg");
                drawingLine(colorsOfLines, justCoordinatesOfStations);
                textingStations(justCoordinatesOfStations, justNamesOfStations);
                StdDraw.enableDoubleBuffering();
                for (int i = 0; i < coordinateOfStationsWeWillUse.size(); i += 2) {
                    double x1 = coordinateOfStationsWeWillUse.get(i);
                    double y1 = coordinateOfStationsWeWillUse.get(i + 1);
                    if (i != coordinateOfStationsWeWillUse.size() - 2) {
                        stationsWhichIsPassed.add(x1);
                        stationsWhichIsPassed.add(y1);
                    }
                    drawingCurrentCircle(x1, y1);
                    StdDraw.pause(pauseDuration);
                    StdDraw.clear();
                    StdDraw.picture(512, 241, "background.jpg");
                    drawingLine(colorsOfLines, justCoordinatesOfStations);
                    textingStations(justCoordinatesOfStations, justNamesOfStations);
                    drawingPreviousCircles(stationsWhichIsPassed);
                    if (i == coordinateOfStationsWeWillUse.size() - 2) {
                        drawingCurrentCircle(x1, y1);
                        StdDraw.show();
                    }
                }
            }
        }
    }


    // Methods for algorithm
    public static boolean checkStation(String station, ArrayList<ArrayList<String>> namesOfStations) {
        boolean isTrue = false;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < namesOfStations.get(i).size(); j++) {
                if (namesOfStations.get(i).get(j).charAt(0) == '*') {
                    String withoutStar = namesOfStations.get(i).get(j).substring(1);
                    if (withoutStar.equals(station)) {
                        isTrue = true;
                        break;
                    }
                } else {
                    if (namesOfStations.get(i).get(j).equals(station)) {
                        isTrue = true;
                        break;
                    }
                }
            }
        }
        return isTrue;
    }

    public static ArrayList<ArrayList<String>> neighbourLines(ArrayList<String> listOfLineNames, ArrayList<ArrayList<String>> breakPoints) {
        ArrayList<ArrayList<String>> neighbourLines = new ArrayList<ArrayList<String>>(10);
        for (int i = 0; i < 10; i++) {
            neighbourLines.add(new ArrayList<String>());
        }
        for (int i = 0; i < listOfLineNames.size(); i++) {
            String station = listOfLineNames.get(i);
            for (int j = 0; j < breakPoints.size(); j++) {
                for (int k = 0; k < breakPoints.get(j).size(); k++) {
                    if (station.equals(breakPoints.get(j).get(k))) {
                        for (int d = 1; d < breakPoints.get(j).size(); d++) {
                            if (!breakPoints.get(j).get(d).equals(station)) {
                                neighbourLines.get(i).add(breakPoints.get(j).get(d));
                            }
                        }
                    }
                }
            }
        }
        return neighbourLines;
    }

    public static void findingPathAlgorithm(int currentIndex, boolean[] visitedLines, ArrayList<String> path1, int terminalIndex, ArrayList<ArrayList<String>> neighbourLines, ArrayList<String> lineNames, int pathIndex, ArrayList<String> path2) {

        if (currentIndex == terminalIndex) {
            for (int i = 0; i < path1.size(); i++) {
                path2.add(path1.get(i));
            }
            return;
        }
        visitedLines[currentIndex] = true;
        for (String neighbour : neighbourLines.get(currentIndex)) {
            int neighbourIndex = lineNames.indexOf(neighbour);
            int previousLine = lineNames.indexOf(path1.get(pathIndex));
            if (path1.size() == 1) {
                if (!visitedLines[neighbourIndex]) {
                    // add the neighbour to the path and use recursion for explore other neighbour lines
                    path1.add(neighbour);
                    findingPathAlgorithm(neighbourIndex, visitedLines, path1, terminalIndex, neighbourLines, lineNames, pathIndex, path2);
                    path1.remove(path1.size() - 1);
                }
            } else {
                if (!visitedLines[neighbourIndex] && !neighbourLines.get(previousLine).contains(neighbour)) {
                    // add the neighbour to the path and use recursion for explore other neighbour lines
                    path1.add(neighbour);
                    findingPathAlgorithm(neighbourIndex, visitedLines, path1, terminalIndex, neighbourLines, lineNames, pathIndex + 1, path2);
                    path1.remove(path1.size() - 1);
                }
            }
        }
    }

    public static void drawingLine(ArrayList<ArrayList<String>> colorsForLines, ArrayList<ArrayList<String>> coordinatesOfStations) {
        for (int i = 0; i < 10; i++) {
            StdDraw.setPenRadius(0.012);
            StdDraw.setPenColor(Integer.parseInt(colorsForLines.get(i).get(0)), Integer.parseInt(colorsForLines.get(i).get(1)), Integer.parseInt(colorsForLines.get(i).get(2)));
            for (int j = 0; j < coordinatesOfStations.get(i).size() - 3; j += 2) {
                int x1 = Integer.parseInt(coordinatesOfStations.get(i).get(j));
                int y1 = Integer.parseInt(coordinatesOfStations.get(i).get(j + 1));
                int x2 = Integer.parseInt(coordinatesOfStations.get(i).get(j + 2));
                int y2 = Integer.parseInt(coordinatesOfStations.get(i).get(j + 3));
                StdDraw.line(x1, y1, x2, y2);
            }
        }
        StdDraw.show();
    }

    public static void textingStations(ArrayList<ArrayList<String>> coordinatesForStations, ArrayList<ArrayList<String>> nameOfStations) {
        for (int i = 0; i < 10; i++) {

            for (int j = 0; j < coordinatesForStations.get(i).size() - 1; j += 2) {
                StdDraw.setPenRadius(0.01);
                StdDraw.setPenColor(StdDraw.WHITE);
                int x0 = Integer.parseInt(coordinatesForStations.get(i).get(j));
                int y0 = Integer.parseInt(coordinatesForStations.get(i).get(j + 1));
                StdDraw.circle(x0, y0, 0.5);
                String station = nameOfStations.get(i).get(j / 2);
                if (station.charAt(0) == '*') {
                    StdDraw.setPenColor(StdDraw.BLACK);
                    String station2 = station.substring(1);
                    StdDraw.setPenRadius(0.012);
                    StdDraw.setFont(new Font("Helvetica", Font.BOLD, 8));
                    StdDraw.text(x0, y0 + 5, station2);
                }
            }
        }
        StdDraw.show();

    }

    public static void drawingPreviousCircles(ArrayList<Double> coordinates) {
        StdDraw.setPenRadius(0.02);
        StdDraw.setPenColor(StdDraw.PRINCETON_ORANGE);
        for (int i = 0; i <= coordinates.size() - 2; i += 2) {
            StdDraw.filledCircle(coordinates.get(i), coordinates.get(i + 1), 2.5);
        }
        StdDraw.show();
    }

    public static void drawingCurrentCircle(double x1, double y1) {
        StdDraw.setPenColor(StdDraw.PRINCETON_ORANGE);
        StdDraw.setPenRadius(0.02);
        StdDraw.filledCircle(x1, y1, 5);
        StdDraw.show();
    }
}
