package com.mcafee.mam.auto.infra.log.html;

import java.io.File;
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import com.hp.gagawa.java.elements.Div;

public class HtmlPackageSummary extends HtmlSummary
{
	public HtmlPackageSummary(File file)
	{
		super(file);
	}

	public void build(GeneralStatistics generalStatistics, TestStatistics testStatistics)
	{
		initPage();
		createHtmlSummaryHeader();
		this.testOrderList.add(testStatistics);
		Collections.sort(testOrderList, new Comparator<TestStatistics>()
		{
			@Override
			public int compare(TestStatistics o1, TestStatistics o2)
			{
				return Collator.getInstance().compare(o1.getPkgName(), o2.getPkgName());
			}
		});
		Div div2 = buildStatisticsTable();
		Div div1 = createGeneralStatisticsTable(generalStatistics);

		body.appendChild(div1);
		body.appendChild(div2);
		save();
	}

	private Div buildStatisticsTable()
	{
		Div div = new Div();
		this.index = 1;
		List<TestStatistics> tsl = new LinkedList<TestStatistics>();
		String lastPackage = "";
		for (TestStatistics ts : this.testOrderList)
		{
			this.totalRunningTime += ts.getRunningTime();
			if (!lastPackage.isEmpty())
			{
				if (!lastPackage.equals(ts.getPkgName()))
				{
					div.appendChild(createHtmlSortHeader(lastPackage));
					div.appendChild(createHtmlTestStatisticsTable(tsl));
					tsl.clear();
				}
			}
			tsl.add(ts);
			lastPackage = ts.getPkgName();
		}
		if (!tsl.isEmpty())
		{
			div.appendChild(createHtmlSortHeader(lastPackage));
			div.appendChild(createHtmlTestStatisticsTable(tsl));
		}
		return div;
	}
}
