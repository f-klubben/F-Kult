package dk.fklub.fkult.business.services.shuffleFilter;

import java.util.*;

public class ShuffleFilter {

    // Weighted shuffle filter, for both themes and sound samples
    public <T extends HasUserId> List<T> weightedShuffle(List<T> list) {
        System.out.println("Starting weighted shuffle filter...");

        List<T> copy = new ArrayList<>(list);
        List<T> shuffledList = new ArrayList<>();
        //Object<Long, T> tempList = new Object();

        // Count the number of times a user appears
        List<T> totalUsers = new ArrayList<>();
        for (T item : copy) {
            boolean found = false;

            for (T existing : totalUsers) {
                if (existing.getUsersId().equals(item.getUsersId())) {
                    existing.setUserCount(existing.getUserCount() + 1);
                    found = true;
                    break;
                }
            }

            if (!found) {
                item.setUserCount(1);
                totalUsers.add(item);
            }
        }
        
        // Calc the weight [%] a user represent
        double counter = copy.size();
        for (T item : totalUsers) {
            double weight = (item.getUserCount() / counter) * 10000;
            item.setUserWeight(weight);
        }

        // Start random number generater between 0 - 10000 and find winner
        Random r = new Random();
        while (totalUsers.size() > 1) {
            int rNumber = r.nextInt(10000);
            double cumulative = 0.0;
            
            for (T u : totalUsers) {
                cumulative += u.getUserWeight();

                if (rNumber < cumulative) {
                    // Find userId corresponding to the first of its entries
                    for (T j: copy) {
                        if (u.getUsersId().equals(j.getUsersId())) {
                            shuffledList.add(j);
                            copy.remove(j);

                            // Subtract/delete a userCount, to prevent searching for users entries when it has no left
                            if (u.getUserCount() != 1) {
                                u.setUserCount(u.getUserCount() - 1);
                            } else {
                                totalUsers.remove(u);
                            }
                            break;
                        }
                    }
                    break;
                }
            }
        }

        // Inserting the rest
        System.out.println("and inserting leftovers");
        for (T j: copy) {
            shuffledList.add(j);
        }

        System.out.println("Done");
        return shuffledList;
    }

    // Quick shuffle filter
    public <T> List<T> quickShuffle(List<T> list) {
        System.out.println("Starting quick shuffle filter...");
        List<T> copy = new ArrayList<>(list);
        Collections.shuffle(copy);
        System.out.println("Done");
        return copy;
    }
}