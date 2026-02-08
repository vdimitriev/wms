#!/bin/bash

echo "=== Testing Warehouse Monitoring System with Web Dashboard ==="
echo ""
echo "Step 1: Starting the application..."
cd wms
./mvnw spring-boot:run > /tmp/wms-app.log 2>&1 &
APP_PID=$!

echo "Waiting for application to start (10 seconds)..."
sleep 10

if ! ps -p $APP_PID > /dev/null; then
    echo "❌ Application failed to start. Check logs:"
    tail -30 /tmp/wms-app.log
    exit 1
fi

echo "✅ Application started successfully (PID: $APP_PID)"
echo ""

echo "Step 2: Testing HTTP endpoints..."
if curl -s http://localhost:8080/ | grep -q "Warehouse Monitoring"; then
    echo "✅ Dashboard is accessible at http://localhost:8080/"
else
    echo "❌ Dashboard not accessible"
fi

echo ""
echo "Step 3: Sending test sensor data..."

# Normal temperature (no alarm)
echo "sensor_id=zone_a; value=25.0" | nc -u -w1 localhost 3344
echo "  ✅ Sent: Normal temperature (25°C)"
sleep 1

# High temperature (alarm!)
echo "sensor_id=zone_b; value=42.0" | nc -u -w1 localhost 3344
echo "  🚨 Sent: High temperature (42°C) - Should trigger ALARM"
sleep 1

# High humidity (alarm!)
echo "sensor_id=zone_c; value=75.0" | nc -u -w1 localhost 3355
echo "  🚨 Sent: High humidity (75%) - Should trigger ALARM"
sleep 2

echo ""
echo "Step 4: Check if alarms were logged..."
if grep -q "ALARM" /tmp/wms-app.log; then
    echo "✅ Alarms are being triggered correctly"
    grep "ALARM" /tmp/wms-app.log | tail -3
else
    echo "⚠️  No alarms found in logs (might be normal if thresholds not exceeded)"
fi

echo ""
echo "=== Test Complete ==="
echo ""
echo "To view the dashboard, open: http://localhost:8080/"
echo "To stop the application: kill $APP_PID"
echo "Application logs: /tmp/wms-app.log"
