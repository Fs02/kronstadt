# Kronstadt

Experiment with spring and coroutines.

## Webmvc vs Webflux using MySQL

| Server  | DB Mode        | Request/Second (mean) | Time/Request (mean) | Time/Request (mean all) | 99%  | 100% |
| ------- | -------------- | --------------------- | ------------------- | ----------------------- | ---- | ---- |
| webmvc  | basic          | 57.23                 | 2236.401            | 17.472                  | 2322 | 4349 |
| webmvc  | async          | 48.39                 | 2645.362            | 20.667                  | 4412 | 5052 |
| webmvc  | dispatchers.IO | 61.96                 | 2065.894            | 16.14                   | 2893 | 3797 |
| webflux | basic          | 57.34                 | 2232.365            | 17.44                   | 2706 | 2902 |
| webflux | async          | 47.49                 | 2695.29             | 21.057                  | 3809 | 5335 |
| webflux | dispatchers.IO | 60.32                 | 2122.104            | 16.579                  | 2914 | 3980 |
| webflux | r2dbc          | 58.32                 | 2194.894            | 17.148                  | 3784 | 4157 |
