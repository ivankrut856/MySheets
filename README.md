# App build

## System requirements

You will need maven and JDK 17+ to run the app.

## Run

If you're on UNIX, simply ./run.sh to compile and start the app.

It's also been tested to run on Windows while starting from a UNIX shell (git shell or WSL)

Use ./run_tests.sh to run tests.

# Using app

## User ~~manual~~ tips

* If there's an error in cell, right click will display message box with full text.

* You see the formula in cell if it's selected, otherwise the value.
* You may expect number operations to behave like in Java Double spec.
## List of supported functions and operators
* +, -, *, /
  * Minus can also be used in unary form
  * "=5+-5" is accepted and has a value of 0.
* sin(x), max(x, y), pow(x, y), pi()
  * In simple cases one could use braceless form: sin 4; pi; sin pi.


## Known problems and limitations
* The table is limited to 1000 * 1000 size ATM, so don't expect for some long cell references to be accepted.
  "=AAAAAAAAAAAAAAA9999999999999999999999999999 + 2" is invalid.
* The 1000 * 1000 limitation might be lifted by adding memory indices to internal representation of cells, but it's already complicated to some degree. So, if the support for bigger table's desired, please, contact user support and request a premium license.
* On some systems, internal swing file chooser's unable to enter a folder where the files with non-ascii symbols are. Our team resolved the problem in most cases, but if you encounter it, don't hesitate to contact user support.
* Our macOS users reported some design inconsistencies, so the recommended OSes are Linux and Windows.