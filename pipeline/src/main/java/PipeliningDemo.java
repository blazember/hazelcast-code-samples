/*
 * Copyright (c) 2008-2018, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Pipeline;

import java.util.Random;

public class PipeliningDemo {

    private HazelcastInstance member;
    private HazelcastInstance client;
    private IMap<Integer, String> map;
    private int keyDomain = 100000;
    private int iterations = 500;
    private int getsPerIteration = 1000;

    public static void main(String[] args) throws Exception {
        PipeliningDemo main = new PipeliningDemo();
        main.init();
        main.pipelined(5);
        main.pipelined(10);
        main.pipelined(100);
        main.nonPipelined();
        System.exit(0);
    }

    private void nonPipelined() {
        System.out.println("Starting non pipelined");
        long startMs = System.currentTimeMillis();
        Random random = new Random();
        for (int i = 0; i < iterations; i++) {
            for (long k = 0; k < getsPerIteration; k++) {
                int key = random.nextInt(keyDomain);
                map.get(key);
            }
        }
        long duration = System.currentTimeMillis();
        System.out.println("Non pipedlined duration:" + (duration - startMs) + " ms");
    }

    private void pipelined(int depth) throws Exception {
        System.out.println("Starting pipelined with depth:"+depth);
        long startMs = System.currentTimeMillis();
        Random random = new Random();
        for (int i = 0; i < iterations; i++) {
            Pipeline pipeline = new Pipeline(depth);
            for (long k = 0; k < getsPerIteration; k++) {
                int key = random.nextInt(keyDomain);
                pipeline.add(map.getAsync(key));
            }
            pipeline.results();
        }
        long duration = System.currentTimeMillis();
        System.out.println("Pipedlined with depth:" + depth + ", duration:" + (duration - startMs) + " ms");
    }

    private void init() {
        member = Hazelcast.newHazelcastInstance();
        client = HazelcastClient.newHazelcastClient();
        map = client.getMap("map");

        for (long l = 0; l < keyDomain; l++) {
            // directly insert on member to speed up insert
            member.getMap(map.getName()).put(l, "" + l);
        }
    }
}
