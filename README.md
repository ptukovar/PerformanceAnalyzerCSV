# PerformanceAnalyzerCSV

Konzolová Java aplikace, která načítá CSV soubor s výkony a generuje grafy průměrných výkonů pomocí knihovny JFreeChart.

## Argumenty
`FROM` — počáteční období ve formátu yyyyMM

`TO` — koncové období ve formátu yyyyMM

`DAY` — volitelný parametr, den v týdnu (1 = pondělí … 7 = neděle)

## Info
Pokud jsou zadány pouze argumenty `FROM` a `TO`, program vygeneruje graf průměrných **měsíčních výkonů**.  
Pokud je přidán i volitelný argument `DAY`, vygeneruje se graf průměrných **denních výkonů** pro daný den v týdnu v určeném období.






