/*
 *  Copyright 2017 original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package io.igx.commons.cv.color;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import io.igx.commons.cv.model.ClusterableColor;
import io.igx.commons.cv.model.ColorStats;
import org.apache.commons.math4.ml.clustering.CentroidCluster;
import org.apache.commons.math4.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math4.ml.distance.EuclideanDistance;
import org.apache.commons.rng.core.source32.JDKRandom;


/**
 * @author Vinicius Carvalho
 */
public class KMeansColorScanner {

	private final Integer clusters;
	private final Integer steps;
	private KMeansPlusPlusClusterer<ClusterableColor> kmeansCluster;


	public KMeansColorScanner(Integer clusters) {
		this(clusters, 10);
	}

	public KMeansColorScanner(){
		this(8, 10);
	}

	public KMeansColorScanner(Integer clusters, Integer steps){
		this.clusters = clusters;
		this.steps = steps;
		this.kmeansCluster = new KMeansPlusPlusClusterer<ClusterableColor>(clusters,1000, new EuclideanDistance(), new JDKRandom(1000L));
	}

	public static List<ClusterableColor> scan(InputStream in, Integer step) throws Exception{
        System.out.println("Scanning image with steps: " + step);
		BufferedImage image = ImageIO.read(in);
		List<ClusterableColor> points = new ArrayList<>((image.getHeight()*image.getWidth())/step);

		for(int i=0;i<image.getWidth();i+=step) {
			for (int j = 0; j < image.getHeight(); j += step) {
				points.add(new ClusterableColor(new Color(image.getRGB(i,j))));
			}
		}
		return points;
	}

	public List<ColorStats> colorStats(InputStream in) throws Exception {
        System.out.println("Preparing to scan image");
		List<ClusterableColor> points = scan(in,this.steps);
		List<CentroidCluster<ClusterableColor>> clusters = kmeansCluster.cluster(points);
		List<ColorStats> colorStats = new ArrayList<>();
		for (CentroidCluster<ClusterableColor> cluster : clusters) {

			double[] center = cluster.centroid().getPoint();
			Color color = new Color((int)center[0],(int)center[1],(int)center[2]);
			colorStats.add(new ColorStats(color,cluster.getPoints().size()));
		}
		colorStats.sort((o1, o2) -> o2.getCounter().compareTo(o1.getCounter()));
		return colorStats;
	}
}
