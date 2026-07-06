import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TelemetryService } from '../../services/telemetry';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dasboard.html',
  styleUrls: ['./dasboard.css']
})
export class DashboardComponent implements OnInit, OnDestroy {

  // Array to store tracking logs arriving from the Spring Boot backend
  public trackingEvents: any[] = [];
  private streamSubscription!: Subscription;

  constructor(private telemetryService: TelemetryService) { }

  ngOnInit(): void {
    console.log('[FRONTEND LOG] Subscribing to real-time security telemetry stream...');
    
    this.streamSubscription = this.telemetryService.getLiveTelemetry().subscribe({
      next: (event) => {
        console.log('[FRONTEND LOG] New target event received:', event);
        // Add new events to the top of the list so recent objects show first
        this.trackingEvents.unshift(event);
      },
      error: (err) => {
        console.error('[FRONTEND LOG] Stream connection encountered an error:', err);
      }
    });
  }

  ngOnDestroy(): void {
    // Unsubscribe when the component is destroyed to prevent memory leaks
    if (this.streamSubscription) {
      this.streamSubscription.unsubscribe();
    }
  }
}