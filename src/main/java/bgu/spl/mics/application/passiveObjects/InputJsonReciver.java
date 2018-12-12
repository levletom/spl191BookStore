package bgu.spl.mics.application.passiveObjects;

public class InputJsonReciver {
    BookInventoryInfo[] initialInventory;
    Resource[] initialResources;
    Service services;

    public BookInventoryInfo[] getInitialInventory() {
        return initialInventory;
    }

    public Resource[] getInitialResources() {
        return initialResources;
    }

    public Service getServices() {
        return services;
    }

    public class Service {
        private TimeHolder time;
        int selling;
        int inventoryService;
        int logistics;
        int resourcesService;
        JCustomer[] customers;

        public TimeHolder getTime() {
            return time;
        }

        public int getSelling() {
            return selling;
        }

        public int getInventoryService() {
            return inventoryService;
        }

        public int getLogistics() {
            return logistics;
        }

        public int getResourcesService() {
            return resourcesService;
        }

        public JCustomer[] getCustomers() {
            return customers;
        }

        private class TimeHolder {
           private int speed;
           private int duration;

            public int getSpeed() {
                return speed;
            }

            public int getDuration() {
                return duration;
            }
        }
    }

    public class Resource {
        private DeliveryVehicle[] vehicles;


        public DeliveryVehicle[] getVehicles() {
            return vehicles;
        }
    }

    private class JCustomer {
        int id;
        String name;
        String address;
        int distance;
        CreditCard creditCard;
        Order[] orderSchedule;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getAddress() {
            return address;
        }

        public int getDistance() {
            return distance;
        }

        public CreditCard getCreditCard() {
            return creditCard;
        }

        public Order[] getOrderSchedule() {
            return orderSchedule;
        }

        private class CreditCard {
            int amount;
            int number;

            public int getAmount() {
                return amount;
            }

            public int getNumber() {
                return number;
            }
        }
    }
}
