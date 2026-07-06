import { Injectable, NgZone } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class TelemetryService {

  private streamUrl = 'http://localhost:8080/api/v1/security/stream';

  constructor(private zone: NgZone) { }

  // Listen to the real-time Server-Sent Events stream
  getLiveTelemetry(): Observable<any> {
    return new Observable(observer => {
      const eventSource = new EventSource(this.streamUrl);

      // Triggered when the backend pushes a new JSON payload via emitter.send()
      eventSource.onmessage = (event) => {
        this.zone.run(() => {
          const data = JSON.parse(event.data);
          observer.next(data);
        });
      };

      // Triggered if the connection drops or errors out
      eventSource.onerror = (error) => {
        this.zone.run(() => {
          observer.error(error);
        });
      };

      // Clean up the connection if the component destroys / unsubscribes
      return () => {
        eventSource.close();
      };
    });
  }
}