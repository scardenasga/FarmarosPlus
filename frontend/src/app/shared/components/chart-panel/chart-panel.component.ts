import { CommonModule } from '@angular/common';
import { Component, computed, input } from '@angular/core';
import { NgChartsModule } from 'ng2-charts';
import { ChartData, ChartDataset, ChartOptions, ChartType } from 'chart.js';

@Component({
  selector: 'app-chart-panel',
  standalone: true,
  imports: [CommonModule, NgChartsModule],
  templateUrl: './chart-panel.component.html',
  styleUrl: './chart-panel.component.css'
})
export class ChartPanelComponent {
  title = input<string>('');
  subtitle = input<string>('');
  badge = input<string>('');
  chartType = input<ChartType>('bar');
  chartData = input.required<ChartData<ChartType, number[], string>>();
  chartOptions = input<ChartOptions<ChartType> | undefined>(undefined);
  showLegend = input<boolean>(false);
  height = input<string>('280px');
  emptyMessage = input<string>('Sin datos para mostrar.');

  hasData = computed(() =>
    this.chartData().datasets.some((dataset: ChartDataset<ChartType, number[]>) =>
      Array.isArray(dataset.data) && dataset.data.some((value: number) => typeof value === 'number' && !Number.isNaN(value))
    )
  );
}
