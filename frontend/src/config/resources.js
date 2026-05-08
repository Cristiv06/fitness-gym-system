export const resources = [
  {
    key: "members",
    label: "Members",
    endpoint: "/api/members",
    idField: "memberId",
    fields: [
      { key: "email", label: "Email", type: "text", required: true },
      { key: "fullName", label: "Full Name", type: "text", required: true },
      { key: "phone", label: "Phone", type: "text" },
      { key: "dateOfBirth", label: "Date of Birth", type: "date" }
    ]
  },
  {
    key: "member-profiles",
    label: "Member Profiles",
    endpoint: "/api/member-profiles",
    idField: "memberId",
    fields: [
      { key: "memberId", label: "Member ID", type: "number", required: true },
      { key: "emergencyContact", label: "Emergency Contact", type: "text" },
      { key: "notes", label: "Notes", type: "textarea" }
    ]
  },
  {
    key: "membership-plans",
    label: "Membership Plans",
    endpoint: "/api/membership-plans",
    idField: "planId",
    fields: [
      { key: "name", label: "Name", type: "text", required: true },
      { key: "durationMonths", label: "Duration (months)", type: "number", required: true },
      { key: "price", label: "Price", type: "number", required: true, step: "0.01" },
      { key: "description", label: "Description", type: "textarea" }
    ]
  },
  {
    key: "subscriptions",
    label: "Subscriptions",
    endpoint: "/api/subscriptions",
    idField: "subscriptionId",
    fields: [
      { key: "memberId", label: "Member ID", type: "number", required: true },
      { key: "planId", label: "Plan ID", type: "number", required: true },
      { key: "startDate", label: "Start Date", type: "date", required: true },
      { key: "endDate", label: "End Date", type: "date", required: true },
      {
        key: "status",
        label: "Status",
        type: "select",
        options: ["ACTIVE", "EXPIRED", "CANCELLED"]
      }
    ]
  },
  {
    key: "trainers",
    label: "Trainers",
    endpoint: "/api/trainers",
    idField: "trainerId",
    fields: [
      { key: "fullName", label: "Full Name", type: "text", required: true },
      { key: "specialization", label: "Specialization", type: "text" },
      { key: "phone", label: "Phone", type: "text" },
      { key: "email", label: "Email", type: "text" }
    ]
  },
  {
    key: "equipment",
    label: "Equipment",
    endpoint: "/api/equipment",
    idField: "equipmentId",
    fields: [{ key: "name", label: "Name", type: "text", required: true }]
  },
  {
    key: "rooms",
    label: "Rooms",
    endpoint: "/api/rooms",
    idField: "roomId",
    fields: [
      { key: "name", label: "Name", type: "text", required: true },
      { key: "maxCapacity", label: "Max Capacity", type: "number", required: true },
      { key: "equipmentIds", label: "Equipment IDs (comma separated)", type: "csv-number-array" }
    ]
  },
  {
    key: "gym-classes",
    label: "Gym Classes",
    endpoint: "/api/gym-classes",
    idField: "classId",
    fields: [
      { key: "trainerId", label: "Trainer ID", type: "number", required: true },
      { key: "roomId", label: "Room ID", type: "number", required: true },
      { key: "title", label: "Title", type: "text", required: true },
      { key: "startTime", label: "Start Time", type: "datetime-local", required: true },
      { key: "endTime", label: "End Time", type: "datetime-local", required: true },
      { key: "maxParticipants", label: "Max Participants", type: "number", required: true }
    ]
  },
  {
    key: "class-enrollments",
    label: "Class Enrollments",
    endpoint: "/api/class-enrollments",
    idField: "enrollmentId",
    fields: [
      { key: "memberId", label: "Member ID", type: "number", required: true },
      { key: "classId", label: "Class ID", type: "number", required: true }
    ]
  },
  {
    key: "check-ins",
    label: "Check-ins",
    endpoint: "/api/check-ins",
    idField: "checkinId",
    fields: [
      { key: "memberId", label: "Member ID", type: "number", required: true },
      { key: "checkinTime", label: "Check-in Time", type: "datetime-local" }
    ]
  }
];
