package com.money.manager.ex.chart;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.util.SparseArrayCompat;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.money.manager.ex.R;
import com.money.manager.ex.core.Core;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.SeriesSelection;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer.Orientation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Chart extends AbstractChart {

	public View buildIncomeExpensesChart(Context context, String title, double[] incomes, double[] expenses, SparseArrayCompat<String> xTextLabels) {
		// list of values income and expenses
		List<double[]> values = new ArrayList<double[]>();
		// add income and expenses
		values.add(incomes);
		values.add(expenses);
		// create a renderer
		Core core = new Core(context);
		int[] colors = new int[] {context.getResources().getColor(core.resolveIdAttribute(R.attr.holo_blue_color_theme)), context.getResources().getColor(core.resolveIdAttribute(R.attr.holo_red_color_theme))};
		
		XYMultipleSeriesRenderer renderer = buildBarRenderer(colors);
		renderer.setOrientation(Orientation.HORIZONTAL);
		
		if (xTextLabels != null) {
			for(int i = 0; i < xTextLabels.size(); i++) {
				if (xTextLabels.get(i) != null) {
					renderer.addXTextLabel(i + 1, xTextLabels.get(i));
				}
			}
		}
		
		// high value
		double heighestValue = 0;
		// calculate max
		for(double[] item : values) {
			for(int i = 0; i < item.length; i++) {
				if (heighestValue < item[i]) heighestValue = item[i]; 
			}
		}

		// set chart settings
		setChartSettings(context, renderer, title, "", "", 0.5, incomes.length + 0.5, 0, heighestValue + (heighestValue * .2f),
						 context.getResources().getColor(core.resolveIdAttribute(R.attr.theme_axes_color)), context.getResources().getColor(core.resolveIdAttribute(R.attr.theme_labels_color)));

		renderer.setYLabels(7);
		renderer.setXLabels(-1);
		//renderer.setShowLegend(false);
		renderer.setLegendHeight(60);
		renderer.setLegendTextSize(20);
		renderer.setShowAxes(true);
		renderer.setShowGrid(true);
		renderer.setAntialiasing(true);
		
		return ChartFactory.getBarChartView(context, buildBarDataset(new String[] {context.getString(R.string.income), context.getString(R.string.expenses)}, values), renderer, Type.DEFAULT);
	}
	
	public View buildPieChart(final Context context, final String title, final ArrayList<ValuePieChart> values) {
		int[] colors = new int[] { Color.parseColor("#0079EA"), Color.parseColor("#EE2A00"), Color.parseColor("#F79731"), Color.parseColor("#BD7FAE"),
				Color.parseColor("#FFF3AB"), Color.parseColor("#66AE3F"), Color.parseColor("#BB7FB8"), Color.parseColor("#6491AA"),
				Color.parseColor("#E8C145"), Color.parseColor("#2B96E7"), Color.parseColor("#D29AF7"), Color.parseColor("#8FEA7B"),
				Color.parseColor("#FFFF3B"), Color.parseColor("#58CCCC"), Color.parseColor("#7AB33E") };
		
		//sort arraylist
		Collections.sort(values, new Comparator<ValuePieChart>() {

			@Override
			public int compare(ValuePieChart lhs, ValuePieChart rhs) {
				return lhs.getValue() > rhs.getValue() ? -1 : lhs.getValue() == rhs.getValue() ? 0 : 1;
			}
		});
		
		Core core = new Core(context);
		final DefaultRenderer renderer = new DefaultRenderer();
		final CategorySeries series = new CategorySeries(title);
		
		double heighestValue = 0;
		
		// each element of collection 
		for(int i = 0; i < (values.size() < colors.length ? values.size() : colors.length); i ++) {
			ValuePieChart item = values.get(i);
			series.add(item.getCategory(), item.getValue());
			// create simpleseriesrenderer
			SimpleSeriesRenderer simpleSeriesRenderer = new SimpleSeriesRenderer();
			simpleSeriesRenderer.setColor(colors[(series.getItemCount() - 1) % colors.length]);
			// add renderer for serie
			renderer.addSeriesRenderer(simpleSeriesRenderer);
			// max item
			if (heighestValue < item.getValue())
				heighestValue = item.getValue();
		}
		
		renderer.setLabelsColor(context.getResources().getColor(core.resolveIdAttribute(R.attr.theme_labels_color)));
		renderer.setLabelsTextSize(context.getResources().getDimensionPixelSize(R.dimen.chart_label_text_size));
		
		renderer.setLegendTextSize(context.getResources().getDimensionPixelSize(R.dimen.chart_label_text_size));
		
		renderer.setApplyBackgroundColor(true);
		renderer.setBackgroundColor(context.getResources().getColor(core.resolveIdAttribute(R.attr.theme_background_color)));
		
		renderer.setPanEnabled(false);
		//renderer.setZoomEnabled(false);
		// get a piechart
		final GraphicalView chart =  ChartFactory.getPieChartView(context, series, renderer);
		
		// set a click
		renderer.setClickEnabled(true);
		chart.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SeriesSelection seriesSelection = chart.getCurrentSeriesAndPoint();
				if (seriesSelection != null) {
					for(int i = 0; i < series.getItemCount(); i ++) {
						renderer.getSeriesRendererAt(i).setHighlighted(i == seriesSelection.getPointIndex());
					}
					String textToast = "<b>" + values.get(seriesSelection.getPointIndex()).getCategory() + "</b> = " + values.get(seriesSelection.getPointIndex()).getValueFormatted();
					Toast.makeText(context, Html.fromHtml(textToast), Toast.LENGTH_SHORT).show();
					// repaint chart
					chart.repaint();
				}
			}
		});
		
		return chart;
	}
	
}

