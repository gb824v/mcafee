package com.mcafee.mam.auto.infra.log.html;

import java.io.File;
import java.io.IOException;
import com.hp.gagawa.java.elements.Frame;
import com.hp.gagawa.java.elements.Frameset;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HtmlIndex extends HtmlWrite
{

	private final String FRAME_SET_ROWS = "30%,70%";
	private final String FRAME_SET_COLS = "35%,65%";

	public HtmlIndex(File file)
	{
		super.file = file;
		super.setFrameTitle("Qaa Results");
		create();
	}

	private void write() throws IOException
	{
		Frame frameResults = new Frame();
		Frame frameTestList = new Frame();
		Frame frameHierarchy = new Frame();

		frameResults.setName("testFrame");
		frameResults.setSrc("summaryPkg.html");
		
		frameTestList.setName("testsListFrame");
		frameTestList.setSrc("testsList.html");

		frameHierarchy.setName("hierarchyFrame");
		frameHierarchy.setSrc("hierarchy.html");

		Frameset framesetCols = new Frameset();
		framesetCols.setCols(FRAME_SET_COLS);

		Frameset framesetRows = new Frameset();
		framesetRows.setRows(FRAME_SET_ROWS);

		framesetRows.appendChild(frameHierarchy);
		framesetRows.appendChild(frameTestList);

		framesetCols.appendChild(framesetRows);
		framesetCols.appendChild(frameResults);

		html.appendChild(framesetCols);
	}

	public void create()
	{
		try
		{
			write();
			super.save();
		}
		catch (IOException ex)
		{
			Logger.getLogger(HtmlIndex.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

}
