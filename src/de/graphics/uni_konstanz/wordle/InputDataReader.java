package de.graphics.uni_konstanz.wordle;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;

public class InputDataReader {

  public static List<TextItem> loadCSV(final File file) {
    final List<TextItem> res = new ArrayList<TextItem>();

    if(file.exists()) {

      // Read File Line By Line
      float max = Float.MIN_VALUE;
      float min = Float.MAX_VALUE;

      CSVReader reader = null;
      try {
        reader = new CSVReader(new FileReader(file));
        String[] nextLine;
        while((nextLine = reader.readNext()) != null) {
          // nextLine[] is an array of values from the line

          System.out.println(nextLine[0] + nextLine[1] + "etc...");

          if(nextLine.length > 1) {
            final String term = nextLine[0].trim();
            final Float weight = new Float(nextLine[1].trim());
            if(weight > max) {
              max = weight;
            }
            if(weight < min) {
              min = weight;
            }

            final Color color;
            if(nextLine.length > 2) { // we have a color
              final String c = nextLine[2];
              color = new Color(Integer.parseInt(c.trim(), 16));
            } else {
              color = Color.BLACK;
            }

            res.add(new TextItem(term, weight, color));
          }

        }
      } catch(final NumberFormatException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch(final FileNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch(final IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } finally {
        if(reader != null) {
          try {
            reader.close();
          } catch(final IOException e) {
            e.printStackTrace();
          }
        }
      }

      for(final TextItem textItem : res) {
        textItem.setSize((textItem.getSize() - min) / (max - min));
      }

      Collections.sort(res);

    }

    return res;
  }

}
