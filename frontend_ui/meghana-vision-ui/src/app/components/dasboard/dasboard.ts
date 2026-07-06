import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
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

  // Injected ChangeDetectorRef to force UI repaints on asynchronous background events
  constructor(
    private telemetryService: TelemetryService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    console.log('[FRONTEND LOG] Subscribing to real-time security telemetry stream...');
    
    this.streamSubscription = this.telemetryService.getLiveTelemetry().subscribe({
      next: (event) => {
        console.log('[FRONTEND LOG] New target event received:', event);
        
        // 1. Scan the list to find if a card for this target entity name already exists
        const existingIndex = this.trackingEvents.findIndex(e => e.name === event.name);

        if (existingIndex !== -1) {
          // 2. Deduplicate: Rip out the old historical event status snapshot
          this.trackingEvents.splice(existingIndex, 1);
        }

        // 3. Float the updated state transition directly to the top of the viewport
        this.trackingEvents.unshift(event);

        // Force Angular to evaluate the array length and repaint the DOM instantly
        this.cdr.detectChanges();
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