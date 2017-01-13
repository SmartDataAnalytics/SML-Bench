/*
 * Copyright 2016 AKSW.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.aksw.mlbenchmark.validation.measures;

import java.math.BigDecimal;
import java.math.RoundingMode;
import static org.aksw.mlbenchmark.validation.measures.MeasureMethodNumericValued.ROUNDINGMODE;
import static org.aksw.mlbenchmark.validation.measures.MeasureMethodNumericValued.SCALE;


/**
 * Custom class that represent a point in a Cartesian XY plane
 *
 * @author Giuseppe Cota <giuseppe.cota@unife.it>
 */
public class Point {

    // z-axis
    private BigDecimal x;
    // y-axis
    private BigDecimal y;

    public Point(BigDecimal x, BigDecimal y) {
        this.x = x.setScale(SCALE, ROUNDINGMODE);
        this.y = y.setScale(SCALE, ROUNDINGMODE);
    }

    /**
     * @return the x
     */
    public BigDecimal getX() {
        return x;
    }

    /**
     * @return the y
     */
    public BigDecimal getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        return ((Point) o).x.compareTo(this.x) == 0 && ((Point)o).y.compareTo(this.y) == 0;
    }

}
