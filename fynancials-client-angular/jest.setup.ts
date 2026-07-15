// @ngrx libraries ship partially-AOT-compiled (Ivy) code; outside the Angular CLI build pipeline
// (which links them via the Angular Linker) they fall back to JIT, which needs the compiler loaded.
import '@angular/compiler';
